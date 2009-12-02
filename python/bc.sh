#!/bin/sh
touch framework/framework.pyx
rm -rf build
python setup.py build_ext -i