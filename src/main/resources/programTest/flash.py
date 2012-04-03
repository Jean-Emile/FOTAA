#! /usr/bin/env python
"""
    
Usage: python flash.py [options] [input file]

Options:
    -h,  --help          show this help
    -v,  --verbose       show additional information
    -p                   Processor, 
                         available arguments 'ATMEGA168' or 'ATMEGA1280' 
                         default: ATMEGA168
    -i,   --interface    Specify serial interface to broadcast 
                         firmware file over, default /dev/ttyUSB0
    
Examples:
    LScreamer.py -p ATMEGA168 -v -i /dev/ttyUSB0 ~/file.hex   
          Wirelessly downloads file.hex to an ATMEGA168 processor
    LScreamer.py
          Lists this information as well as the available ports on the system.
          This could be ran before inserting serial device, and after, to 
          determine which port it has been connected to.

    """

import serial
import glob
import sys
import getopt

def scan():
    """scan for available ports. return a list of device names."""
    return glob.glob('/dev/tty*') + glob.glob('/dev/ttyUSB*')

def usage():
    print __doc__

class FileProc:
    "File processing utilities"
    
    def __init__(self, fileloc):
        """Main processing occurs within the FileProc class"""
        #define global variables modified in this def
        
        self.setArc()
        pfile = self.parse(fileloc)
        if _parsed_file == 0:
            print "Problem parsing hex file, please check file again"
            sys.exit()
        
        #file parsed successfully, initiate remote reset
        print "Waiting for target IC to boot into bootloader"
        if _verbose == 1:  print "Open port " + _iface + ' with baud rate = 19200, 8 data bits, 1 stop bit, 1 sign bit with a 1 second timeout'
        
        ser = serial.Serial(_iface, 19200, timeout=1)
        
        #toggle RTS line to restart remote device
        ser.setRTS(1)             #set RTS
        ser.setRTS(0)             #Clear RTS
        
        print "   To quit waiting, do Cnrl + C"
        if _verbose == 1:
            print "   If target IC does not boot into bootloader and start outputting a 5, not '5', then the target should be checked"
        while ser.read(1) != '\x05':
            pass
        
        #target is in bootloader, send 6, not '6', to start firmware transmission
        ser.write('\x06')
        print "Target has successfully booted into bootloader,  starting transmission of firmware"
        print ser.read()
        print ser.read()
        print ser.read()
        print ser.read()

        ser.write('K')
        ser.write('0')
        ser.write('0')
        ser.write('0')
        
        current_memory_address = 0
        while current_memory_address < _lastmem:
            #wait until target IC sends confirmation
            while ser.inWaiting():
                pass
            
            target_status = ser.read()
            if target_status == 'T':
                #everything working correctly
                pass
            elif target_status == '\x07':
                #need to resend line
                print "resending last line"
                current_memory_address -= _pagesize
            else:
                print target_status
                print "incorrect response from target IC, will now exit"
                sys.exit()
            
            #show progress in terminal
            print str(current_memory_address) + " of " + str(_lastmem)
            
            #start processing hex file for output
            #convert 16 bit current_memory_address into two 8-bit characters
            memory_address_high = current_memory_address / 256
            memory_address_low = current_memory_address % 256
            
            #calculate checksum of this line
            checksum = 0
            checksum = checksum + _pagesize
            checksum += memory_address_high
            checksum += memory_address_low
            
            for x in range(_pagesize):
                checksum += pfile[current_memory_address + x]
            
            #reduce checksum so that it is only 8 bits
            while checksum > 256:
                checksum -= 256
            
            #take two's compliment of checksum
            checksum = 256 - checksum
            
            #start sending current line to target ic
            #start character
            ser.write(":")
            
            #record length
            if _pagesize < 256:
                ser.write(chr(_pagesize))
            else:  #split up into high and low byte
                ser.write(chr(_pagesize >> 8))
                ser.write(chr(_pagesize % 256))
            
            #send this block's address
            ser.write(chr(memory_address_low))
            ser.write(chr(memory_address_high))
            
            #send this blocks checksum
            ser.write(chr(checksum))
            
            #now send the block
            for x in range(_pagesize):
                ser.write(chr(pfile[current_memory_address + x]))
            
            #update current memory address
            current_memory_address += _pagesize
             
        #we have completed transmitting, tell target ic. Multiple S's for redundancy
        ser.write(":")
        ser.write("S")
        ser.write("S")
        ser.write("S")
        
        #tell user that transmission completed successfully
        print "LScreamer has successfully sent " + str(_lastmem) + " bytes to the target " + _type
        
        #close serial port
        ser.close()
        
        #exit gracefully
            
        

    
    def setArc(self):
        global _memsize
        global _pagesize
        global _parsed_file
        global _maxsize
        global _lastmem
        global _type
        _parsed_file = 0
        if  _type == 'ATMEGA168':
            _memsize = 16384
            _pagesize = 128 #words
            _maxsize = _memsize - 1
        elif _type == 'ATMEGA1280':
            _memsize = 131072
            _pagesize = 256 #words
            _maxsize = _memsize - 1
    

        
    def parse(self, fileloc):
        """remove formatting and checksums, sort into rows of 128 bytes"""
        #define global variables modified in this def
        global _lastmem
        global _parsed_file
        try:
            fhex = open(fileloc,"r")
        except IOError:
            print "File could not be opened"
            sys.exit()
        """file is open, enter loop reading in hex lines"""
        li=[]  #initialize list, so can start adding elements by extending it
        if _verbose == 1: print "reading input file '" + fileloc + "' now."
        while 1:
            lines = fhex.readlines(100000)
            if not lines:
                break
            for line in lines:
                #remove colon and new line
                if(line[0]!=':'):  #if line has no : then ignore
                    continue
                s = line.split(":")
                s = s[1].split("\r\n")
                if(s[7:9]=='04'):
                    continue
                if(len(s[0])!=0):  #remove empty lines
                    li.extend([s[0]])        
        #Hex file is cleaned up now, stored in list li
        #prefill hex_array with 0xFF
        hex_array=[]
        hex_array = [255 for i in range(_memsize)]
        
        if _verbose == 1: print "    processing hex file..."
        
        #step through cleaned file and load into hex array
        for line in li:
            record_length = int(line[0:2], 16)  # int('A',16) returns 10
            
            #find memory address to store this line
            memory_address_high = int(line[2:4], 16)
            memory_address_low = int(line[4:6], 16)
            memory_address = memory_address_high * 256 + memory_address_low
            
            #check for end of file tag
            if int(line[6:8], 16) == 1:
                break
            
            #save last memory location
            _lastmem = memory_address + record_length
            
            for x in range(record_length):  #Save values to 
                lower_byte = 8+x*2
                upper_byte = 10+x*2
                hex_array[memory_address + x]= int(line[lower_byte:upper_byte], 16)
        
        #file was successfully parsed
        fhex.close()
        _parsed_file = 1
        return hex_array
        
                
                
        
def main(argv):
    try:
        #To test this in python, do args = '-hvp ATMEGA168 /home/test'.split()
        #then do getopt.getopt(argv,  "hvp:", ["help", "--verbose"])
        opts,  args = getopt.getopt(argv,  'hvp:i:', ['help', 'verbose', 'interface'])
        #detect if no inputs are given
        if len(args) == 0:
            usage()
            print "\nThe available interfaces are: " + str(scan()) + "\n"
            sys.exit(2)
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    #setup global variables
    global _verbose ; _verbose = 0 
    global _iface ; _iface = '/dev/ttyUSB0'     
    global _type ; _type = 'ATMEGA168'
    
    for opt,  arg in opts:
        if opt in ("-h",  "--help"):
            usage()
            sys.exit()
        if opt in ('-v', '--verbose'):
            _verbose = 1
        if opt in ('-p'):
            _type = arg
        if opt in ('-i', '--interface'):
            _iface = arg
    hex = "".join(args)
    
    FileProc(hex)

    
if __name__=='__main__':
    main(sys.argv[1:])
"""    
 * This program is free software; you can redistribute it and/or modify it     *
 * under the terms of the GNU General Public License as published by the Free  *
 * Software Foundation; either version 3 of the License, or (at your option)   *
 * any later version.                                                          *
 *                                                                             *
 * This program is distributed in the hope that it will be useful, but WITHOUT *
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or       *
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for    *
 * more details.                                                               *
 *                                                                             *
 * You should have received a copy of the GNU General Public License along     *
 * with this program; if not, see <http://www.gnu.org/licenses/>.              *"""
