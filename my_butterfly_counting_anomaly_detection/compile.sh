echo compiling java sources...
rm -rf class
mkdir class

javac -cp ./fastutil-7.2.0.jar:./commons-math3-3.0.jar -d class $(find ./src -name *.java)

echo make jar archive...
cd class
jar cf Butterfly-2.0.jar ./
rm ../Butterfly-2.0.jar
mv Butterfly-2.0.jar ../
cd ..
rm -rf class

echo done.
