#include "Decoder.h"

/* decode a 32-bit machine code into its fields */
Instruction Decoder::decode(int machineCode) {
    Instruction instruction;

    /* extract opcode, regA, regB, destReg, and offset */
    instruction.opcode = (machineCode >> 22) & 0x7;
    instruction.regA = (machineCode >> 19) & 0x7;    
    instruction.regB = (machineCode >> 16) & 0x7;   
    instruction.destReg = machineCode & 0x7;
    instruction.offset = convertNum(machineCode & 0xFFFF);

    return instruction;
}

/* convert 16-bit 2's complement number to 32-bit signed integer */
int Decoder::convertNum(int num) {
    /* check if sign bit is set */
    if (num & (1 << 15)) { 
        num -= (1 << 16); 
    }
    return num;
}