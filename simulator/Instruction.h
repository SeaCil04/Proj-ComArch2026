#ifndef INSTRUCTION_H
#define INSTRUCTION_H

struct Instruction {
    int opcode;
    int regA;
    int regB;
    int destReg;
    int offset;
};

#endif