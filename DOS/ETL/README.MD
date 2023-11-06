## ETI (A Sandra's child project for data operational systems)
ETI stands for Extract transform of invoices

### Building from source
#### Install the shared library tesseract (the ocr lib) in Linux Debian
Additionaly teserract requires a set of propietary fonts that shall be also installed.
```sh
sudo apt install ttf-mscorefonts-installer libtesseract-dev -y
```

#### Trainning data is required
Trained data files shall be found at any folder pointed out by the TESSDATA_PREFIX environment variable.
*We strongly suggest to configure this variable as a regular one for the terminal user's session*.
```sh
echo 'export TESSDATA_PREFIX=$HOME/tessdata' >> ~/.profile
source ~/.profile
```

#### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf -o=../XXXXXX-export_comercial_invoce.xml"
```
> Note: `-i` and `-o` are required to provide a relative or absolute path to the PDF input and XML output.

#### Compile and bundle along with dependencies for later execution
```sh
./gradlew shadowJar
mv ./engine/build/libs/invoice_ocr.jar $INSTALLATION_DIR
```

#### Execution via jre (java runtime)
```sh
java -jar  ./invoice_ocr.jar -i=./907623-PrecioVenta.pdf -o=./907623.xml
```
> Note: `runtime` is distributed by oracle without sdk as well.



