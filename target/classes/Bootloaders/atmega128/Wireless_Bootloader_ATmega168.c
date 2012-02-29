/**********************************************************/
/* Serial Bootloader for ATMEGA328P  AVR Controller       */
/*                                                        */
/* Copyright (c) 2009,  Nathan Seidle                     */
/*                                                        */
/* Hacked 2012 by JeD jedartois@gmail.com                 */
/*                                                        */
/*                                                        */
/*                                                        */
/* This program is free software; you can redistribute it */
/* and/or modify it under the terms of the GNU General    */
/* Public License as published by the Free Software       */
/* Foundation; either version 2 of the License, or        */
/* (at your option) any later version.                    */
/*                                                        */
/* This program is distributed in the hope that it will   */
/* be useful, but WITHOUT ANY WARRANTY; without even the  */
/* implied warranty of MERCHANTABILITY or FITNESS FOR A   */
/* PARTICULAR PURPOSE.  See the GNU General Public        */
/* License for more details.                              */
/*                                                        */
/* You should have received a copy of the GNU General     */
/* Public License along with this program; if not, write  */
/* to the Free Software Foundation, Inc.,                 */
/* 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA */
/*                                                        */
/* Licence can be viewed at                               */
/* http://www.fsf.org/licenses/gpl.txt                    */
/*                                                        */
/* Target =  ATMEGA128                                   */
/**********************************************************/

#include <avr/io.h>
#include <avr/boot.h>
#include <avr/interrupt.h>
#include <avr/pgmspace.h>
#include <util/delay.h>

//Define baud rate
#define BAUD   19200 //Works with internal osc
//#define BAUD   38400 //Works with internal osc
//#define BAUD   57600 //Does not work with internal osc
#define MYUBRR (((((CPU_SPEED * 10) / (16L * BAUD)) + 5) / 10) - 1)

//Here we calculate the wait period inside getch(). Too few cycles and the XBee may not be able to send the character in time. Too long and your sketch will take a long time to boot after powerup.
#define CPU_SPEED	8000000
#define MAX_CHARACTER_WAIT	15 //10 works. 20 works. 5 throws all sorts of retries, but will work.
#define MAX_WAIT_IN_CYCLES ( ((MAX_CHARACTER_WAIT * 8) * CPU_SPEED) / BAUD )

//I have found that flow control is not really needed with this implementation of wireless bootloading.
//Adding flow control for wireless support
//#define sbi(port_name, pin_number)   (port_name |= 1<<pin_number)
//#define cbi(port_name, pin_number)   (port_name &= (uint8_t)~(1<<pin_number))
//#define CTS		2 //This is an input from the XBee. If low, XBee is busy - wait.
//#define RTS		3 //This is an output to the XBee. If we're busy, pull line low to tell XBee not to transmit characters into ATmega's UART

#define TRUE	0
#define FALSE	1

//Status LED
#define LED_DDR  DDRB
#define LED_PORT PORTB
#define LED_PIN  PINB
#define LED      PINB5

//Function prototypes
void putch(char);
char getch(void);
void flash_led(uint8_t);
void onboard_program_write(uint32_t page, uint8_t *buf);
void (*app_start)(void) = 0x0000;
void putchKID(void);
int checkID(void);

//Variables
uint8_t incoming_page_data[256];
uint8_t page_length;
uint8_t retransmit_flag = FALSE;


#define MAX_SIZE_ID 4
const char ID[MAX_SIZE_ID]= "K000";

void putchKID(void)
{
    int i=0;
	for(i=0;i< MAX_SIZE_ID;i++)
	{
	  putch((char)ID[i]);
	}
}

int checkID(void)
{
	int i=0;
	int rt=0;
	for(i=0;i< MAX_SIZE_ID;i++)
	{
		if((char)ID[i] != (char)getch())
		{
			rt =  -1;
		}
	}
return rt;
}

union page_address_union {
	uint16_t word;
	uint8_t  byte[2];
} page_address;

int main(void)
{
	uint8_t check_sum = 0;
	uint16_t i;

    //Setup USART baud rate
    UBRR0H = MYUBRR >> 8;
    UBRR0L = MYUBRR;
    UCSR0B = (1<<RXEN0)|(1<<TXEN0);

	//set LED pin as output
	LED_DDR |= _BV(LED);

	//flash onboard LED to signal entering of bootloader
	flash_led(1);

	//Start bootloading process

	putch(5); //Tell the world we can be bootloaded

	//Check to see if the computer responded
	uint32_t count = 0;
	while(!(UCSR0A & _BV(RXC0)))
	{
		count++;
		if (count > MAX_WAIT_IN_CYCLES)
		{
			app_start();
		}
	}
	if(UDR0 != 6) app_start(); //If the computer did not respond correctly with a ACK, we jump to user's program
	putchKID();
	if(checkID() == -1){ app_start();}
	while(1)
	{
		//Determine if the last received data was good or bad
        if (check_sum != 0) //If the check sum does not compute, tell computer to resend same line
RESTART:
            putch(7); //Ascii character BELL
        else
            putch('T'); //Tell the computer that we are ready for the next line

        while(1) //Wait for the computer to initiate transfer
		{
			if (getch() == ':') break; //This is the "gimme the next chunk" command
			if (retransmit_flag == TRUE) goto RESTART;
		}

        page_length = getch(); //Get the length of this block
		if (retransmit_flag == TRUE) goto RESTART;

      	if (page_length == 'S') //Check to see if we are done - this is the "all done" command
		{
			putch('F');
			boot_rww_enable (); //Wait for any flash writes to complete?
			app_start();
		}

		//Get the memory address at which to store this block of data
		page_address.byte[0] = getch(); if (retransmit_flag == TRUE) goto RESTART;
		page_address.byte[1] = getch(); if (retransmit_flag == TRUE) goto RESTART;

        check_sum = getch(); //Pick up the check sum for error dectection
		if (retransmit_flag == TRUE) goto RESTART;

		for(i = 0 ; i < page_length ; i++) //Read the program data
		{
            incoming_page_data[i] = getch();
			if (retransmit_flag == TRUE) goto RESTART;
		}

        //Calculate the checksum
		for(i = 0 ; i < page_length ; i++)
            check_sum = check_sum + incoming_page_data[i];

        check_sum = check_sum + page_length;
        check_sum = check_sum + page_address.byte[0];
        check_sum = check_sum + page_address.byte[1];

        if(check_sum == 0) //If we have a good transmission, put it in ink
            onboard_program_write((uint32_t)page_address.word, incoming_page_data);
	}

}

#define SPM_PAGESIZE 128
void onboard_program_write(uint32_t page, uint8_t *buf)
{
	uint16_t i;
	uint8_t sreg;

	// Disable interrupts.

	sreg = SREG;
	cli();

	eeprom_busy_wait ();

	boot_page_erase (page);
	boot_spm_busy_wait ();      // Wait until the memory is erased.

	for (i=0; i<SPM_PAGESIZE; i+=2)
	{
		// Set up little-endian word.

		uint16_t w = *buf++;
		w += (*buf++) << 8;

		boot_page_fill (page + i, w);
	}

	boot_page_write (page);     // Store buffer in flash page.
	boot_spm_busy_wait ();       // Wait until the memory is written.

	// Reenable RWW-section again. We need this if we want to jump back
	// to the application after bootloading.

	boot_rww_enable ();

	// Re-enable interrupts (if they were ever enabled).

	SREG = sreg;
}

void putch(char ch)
{
	//Adding flow control - xbee testing
	//while( (PIND & (1<<CTS)) != 0); //Don't send anything to the XBee, it is thinking

	while (!(UCSR0A & _BV(UDRE0)));
	UDR0 = ch;
}

char getch(void)
{
	retransmit_flag = FALSE;

	//Adding flow control - xbee testing
	//cbi(PORTD, RTS); //Tell XBee it is now okay to send us serial characters

	uint32_t count = 0;
	while(!(UCSR0A & _BV(RXC0)))
	{
		count++;
		if (count > MAX_WAIT_IN_CYCLES) //
		{
			retransmit_flag = TRUE;
			break;
		}
	}

	//Adding flow control - xbee testing
	//sbi(PORTD, RTS); //Tell XBee to hold serial characters, we are busy doing other things

	return UDR0;
}

void flash_led(uint8_t count)
{
	uint8_t i;

	for (i = 0; i < count; ++i) {
		LED_PORT |= _BV(LED);
		//_delay_ms(100);
		LED_PORT &= ~_BV(LED);
		//_delay_ms(100);
	}
}
