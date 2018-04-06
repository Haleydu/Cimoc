#!/usr/bin/env fish

set File BrowserFilter-data.xml
echo '<!--Generate By feilong-->' | tee $File

for url in (cat java/com/hiroshi/cimoc/source/*.java | grep "new UrlFilter" | perl -pe 's|.*?"(.*?)".*|\1|');
	echo '<data android:host="'$url'" android:scheme="http"/><data android:host="'$url'" android:scheme="https"/>' | tee -a $File
end

echo '<!--Generate By feilong end-->' | tee -a $File
