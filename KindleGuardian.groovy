#!/usr/bin/env groovy

/**
*   Copyright 2011 Dewi Roberts
*
*   Uses the guardian-for-kindle service provided by Mark Longair
*   http://mythic-beasts.com/~mark/random/guardian-for-kindle/
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU Affero General Public License as
*   published by the Free Software Foundation, either version 3 of the
*   License, or (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU Affero General Public License for more details.
*
*   You should have received a copy of the GNU Affero General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import javax.activation.*
import javax.mail.*
import javax.mail.internet.*

@Grab(group='javax.mail', module='mail', version='1.4.1')
@Grab(group='javax.activation', module='activation', version='1.1.1')

def base = 'http://mythic-beasts.com/~mark/random/guardian-for-kindle/'

def from = 'XXXX@gmail.com'
def to = 'amazon_XXXX@free.kindle.com'
def subject = 'Kindle Guardian Downloader'
def bodyText = ''

def smtpHost = 'smtp.googlemail.com'
def smtpPort = '25'
def smtpUsername = 'XXXX@gmail.com'
def smtpPassword = 'XXXX'

def url = new URL(base+'index.html')
println "loading $url"

def line = url.filterLine { it.indexOf('.mobi</a>') > -1 }.toString()

def filename = (line =~ /href="(.*)">/)[0][1]
println "extracted: $filename"

def fileUrl = (base+filename).toURL()

def props = new Properties()
props << ['mail.smtp.host': smtpHost,
          'mail.smtp.port': smtpPort,
          'mail.smtp.auth': 'true',
          'mail.smtp.starttls.enable': 'true']

def auth = [getPasswordAuthentication: { new PasswordAuthentication(smtpUsername, smtpPassword) }] as Authenticator

def session = Session.getDefaultInstance(props, auth)

def multipart = new MimeMultipart()
multipart.addBodyPart(new MimeBodyPart(text: bodyText))
multipart.addBodyPart(new MimeBodyPart(fileName: filename,
                                       dataHandler: new DataHandler(new URLDataSource(fileUrl) {
	@Override
	public String getContentType() {
		return "application/octet-stream"
	}
})))

def message = new MimeMessage(session)
message.from = new InternetAddress(from)
message.setRecipient Message.RecipientType.TO, new InternetAddress(to)
message.subject = subject
message.sentDate = new Date()
message.content = multipart

println "sending to $to..."
Transport.send(message)

println 'sent.'
