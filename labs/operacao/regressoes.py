"""Reexecuta labs anteriores em saída D6, preservando evidências históricas."""
import importlib.util,shutil,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
def main():
    for d,path in [('3','docs/evidencias/desafio-3/executar-lab.py'),('4','docs/evidencias/desafio-4/executar-lab.py')]:
        spec=importlib.util.spec_from_file_location('regressao_d'+d,ROOT/path);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
        m.OUT=ROOT/'docs/evidencias/desafio-6'/('regressao-d'+d);m.OUT.mkdir(exist_ok=True)
        if d=='4':shutil.copyfile(ROOT/'docs/evidencias/desafio-4/consumidor.py',m.OUT/'consumidor.py')
        m.main();print('Regressão D'+d+' concluída',flush=True)
if __name__=='__main__':main()
