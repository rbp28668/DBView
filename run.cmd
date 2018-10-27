set LIB=%~dp0lib
set CP=%LIB%\xercesImpl.jar
set CP=%CP%;%LIB%\xml-apis.jar
set CP=%CP%;%LIB%\serializer.jar
set CP=%CP%;%LIB%\jtds-1.2.jar
set CP=%CP%;%LIB%\lucene-core-2.2.0.jar
java -splash:%~dp0uk\co\alvagem\dbview\images\background.gif -classpath "%CP%" -jar DBView.jar
