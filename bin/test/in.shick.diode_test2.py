#takes about 2 min for each test

from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

device = MonkeyRunner.waitForConnection()
pause = 0.4

# Installs the Android package. Notice that this method returns a boolean, so you can test
# to see if the installation worked.
#device.installPackage('myproject/bin/MyApplication.apk')

# sets a variable with the package's internal name
package = 'in.shick.diode'
# sets a variable with the name of an Activity in the package
activity = 'in.shick.diode.threads.ThreadsListActivity'
# sets the name of the component to start
runComponent = package + '/' + activity

# Runs the component
device.startActivity(component=runComponent)
#loads VERY slowly
MonkeyRunner.sleep(75)
                                   
# click on the MENU
device.touch(295, 50, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*4)

# sort by
device.touch(175, 295, 'DOWN_AND_UP')
MonkeyRunner.sleep(2)

# hot
device.touch(120, 215, 'DOWN_AND_UP')
MonkeyRunner.sleep(5)

# click on the MENU
device.touch(295, 50, 'DOWN_AND_UP')
MonkeyRunner.sleep(pause*4)

# sort by
device.touch(175, 295, 'DOWN_AND_UP')
MonkeyRunner.sleep(2)

# new
device.touch(100, 265, 'DOWN_AND_UP')
MonkeyRunner.sleep(2)

# new
device.touch(100, 265, 'DOWN_AND_UP')
MonkeyRunner.sleep(2)

MonkeyRunner.sleep(5)

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
