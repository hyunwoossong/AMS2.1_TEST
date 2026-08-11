/*
 *  Copyright (c) 2005 eValley.VS Co.,Ltd. All rights reserved.
 */
package com.trx.evalleyvs.eai.edi;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 */
public class AnsiFAGenerator {
	private DateFormat yymmdd;

	private DateFormat yyyymmdd;

	private DateFormat hhmm;

	private AnsiReader ansiReader;

	private Writer ackStream;

	private String controlDateAndTimeOverride;

	private boolean preambleGenerated = false;

	private String thisGroupControlNumber = "12345";

	private int thisDocumentCount = 0;

	private boolean skipFA;

	private String referencedISA;

	private char delimiter;

	private char terminator;

	private String terminatorSuffix, segmentTerminator;

	public AnsiFAGenerator(AnsiReader ansiReader, Writer ackStream) {
		this.ansiReader = ansiReader;
		this.ackStream = ackStream;
	}

	private void generateAcknowledgementPreamble(String firstSegment,
			String groupSender, String groupReceiver, int groupDateLength,
			String groupVersion) throws IOException {
		if (ackStream == null) {
			return;
		}
		if (preambleGenerated) {
			return;
		}
		referencedISA = firstSegment;
		
		// Note that the initialization of the following items cannot occur
		// in the constructor because ansiReader may not have all of the
		// necessary information at that point.
		delimiter = ansiReader.getDelimiter();
		terminator = ansiReader.getTerminator();
		terminatorSuffix = ansiReader.getTerminatorSuffix();
		segmentTerminator = terminator + terminatorSuffix;

		// The ISA envelope is basically the same as that of the input
		// interchange except for reversal of the sender and receiver addresses.
		String faHeader = firstSegment.substring(0, 32)
				+ firstSegment.substring(51, 70)
				+ firstSegment.substring(32, 51) + firstSegment.substring(70);
		ackStream.write(faHeader);
		ackStream.write(terminatorSuffix);

		// Generate the GS (group)
		ackStream.write("GS" + delimiter + "FA" + delimiter + groupReceiver
				+ delimiter + groupSender + delimiter
				+ controlDateAndTime(groupDateLength) + delimiter
				+ thisGroupControlNumber + delimiter + "X" + delimiter
				+ groupVersion);
		ackStream.write(terminator);
		ackStream.write(terminatorSuffix);

		preambleGenerated = true;
	}

	protected void generateGroupAcknowledgmentHeader(String firstSegment,
			String groupSender, String groupReceiver, int groupDateLength,
			String groupVersion, String groupFunctionCode,
			String groupControlNumber) throws IOException {
		if (ackStream == null) {
			return;
		}
		// Do not generate an FA to acknowledge an FA
		if ("FA".equals(groupFunctionCode)) {
			skipFA = true;
			return;
		}
		skipFA = false;

		generateAcknowledgementPreamble(firstSegment, groupSender,
				groupReceiver, groupDateLength, groupVersion);

		// Generate the ST 997
		thisDocumentCount++;
		ackStream.write("ST" + delimiter + "997" + delimiter + "1");
		ackStream.write(segmentTerminator);

		// Generate the AK1 segment to identify the group being acknowledged
		ackStream.write("AK1" + delimiter + groupFunctionCode + delimiter
				+ groupControlNumber);
		ackStream.write(segmentTerminator);

	}

	protected void generateTransactionAcknowledgment(String transactionCode,
			String controlNumber) throws IOException {
		if ((ackStream == null) || skipFA) {
			return;
		}

		// Generate the AK2 segment to identify the transaction set
		ackStream.write("AK2" + delimiter + transactionCode + delimiter
				+ controlNumber);
		ackStream.write(segmentTerminator);

		// Generate the AK5 segment acknowledging the transaction set
		ackStream.write("AK5" + delimiter + "A");
		ackStream.write(segmentTerminator);

	}

	protected void generateGroupAcknowledgmentTrailer(int docCount)
			throws IOException {
		if ((ackStream == null) || skipFA) {
			return;
		}

		// Generate the AK9 segment to designate acceptance of the entire
		// functional group
		ackStream.write("AK9" + delimiter + "A" + delimiter + docCount
				+ delimiter + docCount + delimiter + docCount);
		ackStream.write(segmentTerminator);

		// Generate the SE to match the ST
		ackStream
				.write("SE" + delimiter + (4 + 2 * docCount) + delimiter + "1");
		ackStream.write(segmentTerminator);

	}

	void generateAcknowledgementWrapup() throws IOException {
		if (ackStream == null) {
			return;
		}
		try{// 2012.02.07 ������ ���� = exception �߰�
		// Generate the GE to match the GS
		ackStream.write("GE" + delimiter + thisDocumentCount + delimiter
				+ thisGroupControlNumber);
		ackStream.write(segmentTerminator);

		// Finish with an IEA corresponding to the ISA
		ackStream.write("IEA" + delimiter + "1" + delimiter
				+ referencedISA.substring(90, 99));
		ackStream.write(segmentTerminator);
		
		
		
		}catch(Exception e){
			
		}finally{
			try{	//2012.02.07 ������ ���� = exception �߰�
			if(ackStream != null){	ackStream.close();}
			}catch(Exception e){}
		}
	}

	public String controlDateAndTime(int dateLength) {
		if (controlDateAndTimeOverride != null) {
			return controlDateAndTimeOverride;
		}

		// Do lazy initializations if needed
		if (yymmdd == null) {
			yymmdd = new SimpleDateFormat("yyMMdd");
		}
		if (yyyymmdd == null) {
			yyyymmdd = new SimpleDateFormat("yyyyMMdd");
		}
		if (hhmm == null) {
			hhmm = new SimpleDateFormat("HHmm");
		}

		Date now = new Date();
		DateFormat sixOrEight = (dateLength == 6) ? yymmdd : yyyymmdd;
		return sixOrEight.format(now) + ansiReader.getDelimiter()
				+ hhmm.format(now);
	}

	/**
	 * Set an override value to be used whenever generating a control date and
	 * time. This method is used only for automated testing.
	 */
	public void setControlDateAndTime(String overrideValue) {
		controlDateAndTimeOverride = overrideValue;
	}

}
