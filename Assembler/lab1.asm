Section  .data
 	flag:	word
	answer:	word
	alpha:	word
	gamma:	word
	C3P0:	word
	R2D2:	word
Section	.code
  	RVALUE	flag
  	GOFALSE	L0
    	LVALUE	answer  
  	RVALUE  alpha
  	PUSH  	2
 	RVALUE 	gamma
  	MPY
  	RVALUE  C3P0
 	RVALUE  R2D2
 	SUB
  	DIV
  	ADD
	STO
	LABEL	L0
	RVALUE	answer
 	PRINT		
	HALT
