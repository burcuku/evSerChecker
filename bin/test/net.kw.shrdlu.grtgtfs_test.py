# Imports the monkeyrunner modules used by this program
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

# Connects to the current device, returning a MonkeyDevice object
device = MonkeyRunner.waitForConnection()

pause = 0.3

# Installs the Android package. Notice that this method returns a boolean, so you can test
# to see if the installation worked.
#device.installPackage('myproject/bin/MyApplication.apk')

# sets a variable with the package's internal name
package = 'net.kw.shrdlu.grtgtfs'

# sets a variable with the name of an Activity in the package
activity = 'net.kw.shrdlu.grtgtfs.SearchActivity'

# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
MonkeyRunner.sleep(5)
                            
# touch edit text field
device.touch(20, 125, MonkeyDevice.DOWN_AND_UP);
MonkeyRunner.sleep(pause)

# write some message
device.type("a");
MonkeyRunner.sleep(pause*5)

#click on the first listed station
device.touch(70, 160, MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(2)

MonkeyRunner.sleep(2)

#click on the first station
device.touch(75, 140, MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(pause*3)

#click on the first time slot
device.touch(75, 140, MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(3)

#press home and close the activity
device.press('KEYCODE_HOME', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(pause*5)

# click to list recent apps
device.press('KEYCODE_APP_SWITCH', MonkeyDevice.DOWN_AND_UP)
MonkeyRunner.sleep(2)

# swipe the app out                                                                             
device.touch(60, 400, MonkeyDevice.DOWN)  

for i in range(1, 12):                                                              
    device.touch(60 + 20 * i, 400, MonkeyDevice.MOVE)                              
    MonkeyRunner.sleep(0.1)             
                                                             

# Remove finger from screen
device.touch(280, 400, MonkeyDevice.UP) 

MonkeyRunner.sleep(2) 
