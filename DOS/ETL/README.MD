## ETI (A Sandra's child project for data operational systems)
ETI stands for Extract transform of invoices

### Building from source
#### Install the shared library tesseract (the ocr lib) in Linux Debian
```sh
sudo apt-get install libtesseract-dev
```

#### Trainning data is required
Trained data files shall be found at any folder pointed out by the TESSDATA_PREFIX environment variable.
We strongly suggest to configure this variable as one of the regulars for the terminal user's session.
```sh
echo 'export TESSDATA_PREFIX=$HOME/tessdata' >> ~/.profile
source ~/.profile
```

#### Compile and run along with needed arguments
```sh
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf -o=../XXXXXX-export_comercial_invoce.xml"
```
> Note: `-i` and `-o` are required to provide a relative or absolute path to the PDF input and XML output.
