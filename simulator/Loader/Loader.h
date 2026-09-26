#ifndef LOADER_H
#define LOADER_H

#include <string>
#include "CPU.h"

class Loader {
public:
    void loadProgram(const std::string& filename, CPU& cpu);
};

#endif