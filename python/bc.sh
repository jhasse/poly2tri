#!/bin/sh
touch framework/framework.pyx
rm framework/*.c
rm -rf build
python setup.py build_ext -i