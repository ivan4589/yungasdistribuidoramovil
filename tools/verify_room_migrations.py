"""Run the actual literal migration SQL with SQLite, without Android or user data.

The historical baseline is frozen at the merged PR #5 commit, not regenerated
from the repaired migration. Android tests additionally validate Room's schema.
"""
import pathlib
import re
import sqlite3
import subprocess

ROOT = pathlib.Path(__file__).resolve().parents[1]
FILE = 'app/src/main/java/com/example/yungasdistribuidora/data/local/database/Migrations.kt'
BASELINE = '2a04318'


def source(ref=None):
    if ref:
        return subprocess.check_output(['git', 'show', f'{ref}:{FILE}'], cwd=ROOT, text=True, encoding='utf-8')
    return (ROOT / FILE).read_text(encoding='utf-8')


def statements(text, version):
    section = text.split(f'val MIGRATION_{version}_{version + 1} =', 1)[1].split('\nval MIGRATION_', 1)[0]
    pattern = r'db\.execSQL\((?:"""(.*?)"""(?:\.trimIndent\(\))?|"([^"\n]*)")\)'
    sqls = []
    for multiline, single in re.findall(pattern, section, re.S):
        sql = multiline or single
        sql = re.sub(r'\$\{maybeColumn\("([^"]+)"\)\}', r'`\1`', sql)
        assert '${' not in sql, 'Unsupported dynamic migration SQL'
        sqls.append(sql)
    assert sqls, 'No migration SQL extracted'
    return sqls


def fixture(fk, version):
    db = sqlite3.connect(':memory:', isolation_level=None)
    db.execute(f'PRAGMA foreign_keys={fk}')
    # Historical CREATE statements at the version-5 release, before nullable fields.
    for sql in statements(source('d918666'), 2):
        db.execute(sql)
    db.execute('ALTER TABLE sales ADD COLUMN cancelledPdfUrl TEXT')
    for n in range(3):
        db.execute('INSERT INTO sales VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)',
                   (f's{n}', f'N{n}', 'c', 'Synthetic', 'PENDING', 'UNPAID', 'CASH',
                    None, 55, 1, 54, 4, 50, 'fixture', None, None, None, '2026', '2026', None))
        for line in range(2):
            db.execute('INSERT INTO sale_details VALUES (?,?,?,?,?,?,?,?)',
                       (f'd{n}-{line}', f's{n}', 'same-product', 'Synthetic', 5, 5.5, 1, 27.5))
        db.execute('INSERT INTO sale_bonuses VALUES (?,?,?,?,?)', (f'b{n}', f's{n}', 'same-product', 'Synthetic', 2))
    db.execute(f'PRAGMA user_version={version}')
    return db


def snapshot(db):
    return {t: db.execute(f'SELECT * FROM {t} ORDER BY id').fetchall()
            for t in ('sales', 'sale_details', 'sale_bonuses')}


def migrate(db, text, version, interrupt=False):
    db.execute('BEGIN')
    try:
        for sql in statements(text, version):
            db.execute(sql)
            if interrupt and sql == 'DROP TABLE sales':
                raise RuntimeError('Synthetic interruption after parent removal')
        assert not db.execute('PRAGMA foreign_key_check').fetchall()
        db.execute(f'PRAGMA user_version={version + 1}')
        db.execute('COMMIT')
    except Exception:
        db.execute('ROLLBACK')
        raise


def main():
    for version in (5, 6):
        for fk in (0, 1):
            with fixture(fk, version) as db:
                before = snapshot(db)
                try:
                    migrate(db, source(BASELINE), version)
                except sqlite3.IntegrityError as error:
                    assert fk == 0 and 'sale_details.id' in str(error)
                    assert before == snapshot(db)
                    print(f'REPRODUCED baseline {version}->{version+1}, FK={fk}: duplicate PK; rollback preserved all rows')
                else:
                    assert fk == 1 and before == snapshot(db)
                    print(f'Baseline {version}->{version+1}, FK={fk}: passed')
            with fixture(fk, version) as db:
                before = snapshot(db)
                for table in before:
                    assert db.execute(f'PRAGMA table_info({table})').fetchall()
                migrate(db, source(), version)
                assert before == snapshot(db)
                assert not db.execute('PRAGMA foreign_key_check').fetchall()
                assert not db.execute('SELECT id FROM sale_details GROUP BY id HAVING count(*) > 1').fetchall()
                assert db.execute('PRAGMA integrity_check').fetchone()[0] == 'ok'
                print(f'PASS repaired {version}->{version+1}, FK={fk}: sales=3 details=6 bonuses=3; FK violations=0')
            with fixture(fk, version) as db:
                before = snapshot(db)
                try:
                    migrate(db, source(), version, interrupt=True)
                except RuntimeError:
                    pass
                else:
                    raise AssertionError('Expected interruption')
                assert before == snapshot(db)
                assert db.execute('PRAGMA user_version').fetchone()[0] == version
                assert not db.execute('PRAGMA foreign_key_check').fetchall()
                print(f'PASS rollback {version}->{version+1}, FK={fk}: complete original data/version restored')


if __name__ == '__main__':
    main()
