"""unittest padrão + relatório JUnit XML, sem plugin Python adicional."""
import sys
import time
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

class Resultado(unittest.TextTestResult):
    def __init__(self,*args):super().__init__(*args);self.casos=[]
    def startTest(self,test):self.inicio=time.monotonic();super().startTest(test)
    def addSuccess(self,test):super().addSuccess(test);self.casos.append((test.id(),"",time.monotonic()-self.inicio))
    def addError(self,test,err):super().addError(test,err);self.casos.append((test.id(),"error",time.monotonic()-self.inicio))
    def addFailure(self,test,err):super().addFailure(test,err);self.casos.append((test.id(),"failure",time.monotonic()-self.inicio))
    def addSkip(self,test,reason):super().addSkip(test,reason);self.casos.append((test.id(),"skipped",0))

if __name__=="__main__":
    suite=unittest.defaultTestLoader.discover("labs/iot/tests")
    r=unittest.TextTestRunner(verbosity=2,resultclass=Resultado).run(suite)
    xml=ET.Element("testsuite",name="PythonGateway",tests=str(r.testsRun),failures=str(len(r.failures)),errors=str(len(r.errors)),skipped=str(len(r.skipped)))
    for nome,status,tempo in r.casos:
        classe,_,method=nome.rpartition(".");case=ET.SubElement(xml,"testcase",classname=classe,name=method,time=str(tempo))
        if status:ET.SubElement(case,status).text="Consultar console Jenkins para detalhes"
    p=Path("reports");p.mkdir(exist_ok=True)
    ET.ElementTree(xml).write(p/"python-junit.xml",encoding="utf-8",xml_declaration=True)
    raise SystemExit(0 if r.wasSuccessful() else 1)
