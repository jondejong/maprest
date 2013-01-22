grails clean
grails package-plugin
cp -v grails-maprest-$1.zip ~/Projects/easyrest/
cd ~/Projects/easyrest/
#grails uninstall-plugin maprest
grails clean
grails install-plugin grails-maprest-$1.zip
cd ~/Projects/maprest/
