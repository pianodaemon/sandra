## ETI (A Sandra's child project for data operational systems)
ETI stands for Extract transform of invoices

### How to configure it first of all
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

### Build and run for debugging purposes
#### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf -o=../XXXXXX-export_comercial_invoce.xml"
```
> Note: `-i` and `-o` are required to provide a relative or absolute path to the PDF input and XML output.


### Build and deploy in production
#### Compile and bundle along with dependencies for later execution
Once the shadowJar has been succesfully compiled, please proceed to move it into your production's path 
```sh
./gradlew shadowJar
mv ./engine/build/libs/invoice_ocr.jar $PRODUCTION_PATH
```

#### Execution via jre (java runtime)
Within production path proceed to execute via the java runtime.
```sh
java -jar  ./invoice_ocr.jar -i=./907623-PrecioVenta.pdf -o=./907623.xml
```
> Note: the `java runtime` is distributed by oracle without sdk.

#### Execution via bourne shell script
System administrators can also wrap the execution through a shell script featuring the following content
```sh
cat <<EOF >>invoice-ocr.sh
#!/bin/sh

INVOICE_OCR_PATH="."
export TESSDATA_PREFIX="./tessdata"

java -jar $INVOICE_OCR_PATH/invoice_ocr.jar $@
EOF
```

Make the script executable
```sh
chmod +x invoice-ocr.sh
```

Then it would be executed as follow
```sh
./invoice-ocr.sh -i=./907623-PrecioVenta.pdf -o=./907623.xml
2023-11-06 17:32:12  INFO  ExpCommercial:58 - Applying corrections to the symbol buffers
2023-11-06 17:32:12  INFO  ExpCommercial:73 - Applying normalizations as needed
2023-11-06 17:32:12  INFO  ExpCommercial:76 - Proceeding to set up the formater finally
2023-11-06 17:32:12  INFO  ExpCommercial:106 - Rendering data as structured information
```
