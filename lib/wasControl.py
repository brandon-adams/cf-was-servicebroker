execfile('wsadminlib.py')
import sys


#print('This is config file that is being used for configuration ' + configFile)
#import javax.xml.parsers.DocumentBuilderFactory as DocumentBuilderFactory
#dbf = DocumentBuilderFactory.newInstance()
#db = dbf.newDocumentBuilder()
#document = db.parse(configFile)
#config = document.getDocumentElement()

def createAppServer(name):
    print createServer('appNode', name)
    print save()
    print listServersOfType(None)


def deleteAppServer(name):
    print deleteServerByNodeAndName('appNode', name)
    print save()
    print listServersOfType(None)

def main():
    if (len(sys.argv) < 2):
        print "This script expects two arguments"
        #print "Below is the process to run the script"
        #print "WEBSPHERE-DMGR-HOME/bin/wsadmin.sh -f config.py config.xml"
        sys.exit()
    
    func = str(sys.argv[0])
    name = str(sys.argv[1])
    
    if (func == "create"):
        createAppServer(name)
    elif (func == "delete"):
        deleteAppServer(name)
    else:
        print "Choose create or delete."

main()
