#ifndef CPU_H
#define CPU_H

#include <cstdint>

class CPU {
public:
    int32_t pc;
    int32_t registers[8];
    int32_t memory[65536];

    CPU();
};

#endif