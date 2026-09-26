#include "CPU.h"

CPU::CPU() {
    pc = 0;

    for (int i = 0; i < 8; i++) {
        registers[i] = 0;
    }
    
    for (int i = 0; i < 65536; i++) {
        memory[i] = 0;
    }
}