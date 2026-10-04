#ifndef INSTRUCTION_H
#define INSTRUCTION_H

/* instruction fields decoded from a 32-bit machine code word */
struct Instruction {
    int opcode;   /* opcode field (bits 24-22) */
    int regA;     /* register A field (bits 21-19) */
    int regB;     /* register B field (bits 18-16) */
    int destReg;  /* destination register field for R-type (bits 2-0) */
    int offset;   /* sign-extended offset field (bits 15-0) */
};

#endif