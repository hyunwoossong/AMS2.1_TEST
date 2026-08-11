package com.trx.evalleyvs.eai.message;

public interface ErrorMessages {
	public final static String ELEMENT_TOO_LONG 			= "Too many characters in an element (delimiter problem?)";
	public final static String EXPECTED_SIMPLE_TOKEN 	= "Expected a simple token";
	public final static String INTERNAL_ERROR_MULTIPLE_EOFS = "Internal error: ANSI interchange previewed more than once";
	public final static String INTERNAL_ERROR_MULTIPLE_PREVIEWS = "End-of-file hit multiple times (internal error)";
	public final static String DIGITS_ONLY = "Element must contain only digits";
	public final static String INVALID_COMPOSITE = "Invalid composite element";
	public final static String UNEXPECTED_EOF = "Unexpected end of data";
	public final static String INVALID_BEGINNING_OF_SEGMENT = "Invalid beginning of segment";
	public final static String X12_MISSING_ISA = "ANSI X.12 interchange must begin with ISA";
	public final static String TOO_MANY_ISA_FIELDS = "Too many fields for an ISA (Segment terminator problem?)";
	public final static String INCOMPLETE_X12 = "Incomplete ANSI X.12 interchange";
	public final static String CONTROL_NUMBER_IEA = "Control number error in IEA segment";
	public final static String CONTROL_NUMBER_GE = "Control number error in GE segment";
	public final static String CONTROL_NUMBER_SE = "Control number error in SE segment";
	public final static String COUNT_IEA = "Functional group count error in IEA segment";
	public final static String COUNT_GE = "Transaction count error in GE segment";
	public final static String COUNT_SE = "Segment count error in SE segment";
	public final static String INVALID_UNA = "Improperly formed UNA segment";
	public final static String CONTROL_NUMBER_UNZ = "Control number error in UNZ segment";
//	public final static String CONTROL_NUMBER_GE = "Control number error in GE segment";
	public final static String CONTROL_NUMBER_UNT = "Control number error in UNT segment";
	public final static String COUNT_UNZ = "Functional group count error in UNZ segment";
//	public final static String COUNT_GE = "Transaction count error in GE segment";
	public final static String COUNT_UNT = "Segment count error in UNT segment";
	public final static String FIRST_SEGMENT_MUST_BE_UNA_OR_UNB = "First segment must be UNA or UNB";
	public final static String NO_HL7_PARSER = "Data begins with MSH indicating HL7 data, but no HL7 parser is available";
	public final static String NO_STANDARD_BEGINS_WITH = "No supported EDI standard interchange begins with ";

}
