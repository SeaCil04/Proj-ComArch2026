#ifndef LOADER_H
#define LOADER_H

#include <string>
#include "stateStruct.h"

class Loader {
public:
    void loadProgram(const std::string& filename, stateStruct& state);
};

#endif