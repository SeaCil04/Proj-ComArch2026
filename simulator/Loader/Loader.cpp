#include "Loader.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <cstdlib>

void Loader::loadProgram(const std::string& filename, stateStruct& state) {
    std::ifstream file(filename);

    if (!file.is_open()) {
        std::cerr << "Error: cannot open file " << filename << std::endl;
        std::exit(1);
    }

    std::string line;

    while (std::getline(file, line)) {

        if (state.numMemory >= 65536) {
            std::cerr << "Error: program is too large" << std::endl;
            std::exit(1);
        }

        std::stringstream ss(line);
        int value;

        if (!(ss >> value)) {
            std::cerr << "Error in reading address "
                      << state.numMemory << std::endl;
            std::exit(1);
        }

        state.memory[state.numMemory] = value;
        state.numMemory++;
    }
}