## ETI (Sandra's project)
ETI stands for Extract transform invoices

#### Building for source
##### Install the shared library tesseract (the ocr lib)
```sh
sudo apt-get install libtesseract-dev
echo 'export TESSDATA_PREFIX=$HOME/tessdata' >> ~/.profile
```

##### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf"
```
> Note: `-i` is required to provide relative or absolute PDF input path.
