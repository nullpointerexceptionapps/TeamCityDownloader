#!/bin/sh

XML_FILES=`find . -name "*.xml"`
NUM_XML_FILES=`echo "$XML_FILES" | wc -l`
XML_LINES=`for f in "$XML_FILES"; do cat $f; done | wc -l`

JAVA_FILES=`find . -name "*.java"`
NUM_JAVA_FILES=`echo "$JAVA_FILES" | wc -l`
JAVA_LINES=`for f in "$JAVA_FILES"; do cat $f; done | wc -l`

TOTAL_FILES=`echo "$NUM_XML_FILES + $NUM_JAVA_FILES" | bc`
TOTAL_LINES=`echo "$XML_LINES + $JAVA_LINES" | bc`

echo "$JAVA_LINES Java lines in $NUM_JAVA_FILES files."
echo "$XML_LINES XML lines in $NUM_XML_FILES files."
echo "-----------"
echo "$TOTAL_LINES lines in $TOTAL_FILES total files."


# verbose?
if [ -n "$1" ]; then
	echo ""
	echo "Files sorted by length (longest first):"
	for f in `echo "$JAVA_FILES"`; do
		LINES=`cat $f | wc -l`
		echo "$f: $LINES lines."
	done | sort -rnk2

	echo "----"
	
	for f in `echo "$XML_FILES"`; do
		LINES=`cat $f | wc -l`
		echo "$f: $LINES lines."
	done | sort -rnk2
fi

