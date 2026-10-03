#include "stateStruct.h"

stateStruct::stateStruct() {
    pc = 0;

    for (int i = 0; i < NUMREGS; i++) {
        registers[i] = 0;
    }

    for (int i = 0; i < NUMMEMORY; i++) {
        memory[i] = 0;
    }

    numMemory = 0;
}