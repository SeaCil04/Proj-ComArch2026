#include "Decoder.h"

Instruction Decoder::decode(int machineCode) {
    Instruction instruction;

    instruction.opcode = (machineCode >> 22) & 0x7;
    instruction.regA = (machineCode >> 19) & 0x7;    
    instruction.regB = (machineCode >> 16) & 0x7;   
    instruction.destReg = machineCode & 0x7;
    instruction.offset = convertNum(machineCode & 0xFFFF);

    return instruction;
}

int Decoder::convertNum(int num) {
    if (num & (1 << 15)) { 
        num -= (1 << 16); 
    }
    return num;
}