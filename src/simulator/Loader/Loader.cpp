#include "Loader.h"
#include <fstream>
#include <iostream>
#include <sstream>
#include <cstdlib>

/* read machine-code file into memory */
void Loader::loadProgram(const std::string& filename, stateStruct& state) {
    std::ifstream file(filename);

    if (!file.is_open()) {
        std::cerr << "Error: cannot open file " << filename << std::endl;
        std::exit(1);
    }

    std::string line;

    /* read lines until end of file */
    while (std::getline(file, line)) {

        /* check if program exceeds memory limit */
        if (state.numMemory >= NUMMEMORY) {
            std::cerr << "Error: program is too large" << std::endl;
            std::exit(1);
        }

        std::stringstream ss(line);
        int value;

        /* read integer from line */
        if (!(ss >> value)) {
            std::cerr << "Error in reading address "
                      << state.numMemory << std::endl;
            std::exit(1);
        }

        state.memory[state.numMemory] = value;
        state.numMemory++;
    }

    file.close();
}