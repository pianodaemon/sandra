## ETI (A Sandra's child project for data operational systems)
ETI stands for Extract transform of invoices

#### Building from source
##### Install the shared library tesseract (the ocr lib)
```sh
sudo apt-get install libtesseract-dev
echo 'export TESSDATA_PREFIX=$HOME/tessdata' >> ~/.profile
```

##### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf -o=../XXXXXX-export_comercial_invoce.xml"
```
> Note: `-i` and `-o` are required to provide a relative or absolute path to the PDF input and XML output.
