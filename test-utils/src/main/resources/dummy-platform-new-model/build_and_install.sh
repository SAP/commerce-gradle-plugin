
zip -r dummy *

mvn install:install-file -Dfile=dummy.zip -DgroupId=de.hybris.platform -DartifactId=hybris-commerce-suite -Dversion=TEST -Dpackaging=zip

touch test.jar
mvn install:install-file -Dfile=test-jar.jar -DgroupId=some.database -DartifactId=jdbc -Dversion=TEST -Dpackaging=jar