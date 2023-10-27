sudo apt-get install libtesseract-dev
export TESSDATA_PREFIX=$PWD/tessdata
./gradlew run --args="-i=../XXXXXX-export_comercial_invoce.pdf"
