## ETI (Code base Sandra)
ETI stands for Extract transform invoices

#### Building for source
##### Install the shared library tesseract (the ocr lib)
```sh
sudo apt-get install libtesseract-dev
echo 'export TESSDATA_PREFIX=$HOME/tessdata' >> ~/.profile
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf"
```

##### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf"
```
