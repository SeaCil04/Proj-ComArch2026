#include "Loader.h"
#include <fstream>

void Loader::loadProgram(const std::string& filename, CPU& cpu) {
    std::ifstream file(filename);

    int value;
    int address = 0;

    while (file >> value && address < 65536) {
        cpu.memory[address] = value;
        address++;
    }
}