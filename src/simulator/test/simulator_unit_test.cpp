#include <iostream>
#include <cassert>
#include "../stateStruct/stateStruct.h"
#include "../Decoder/Decoder.h"
#include "../Executor/Executor.h"
#include "../Instruction.h"

// -------------------------------------------------------------
// Test 1: ทดสอบ Decoder แยกฟิลด์และ Sign Extension
// -------------------------------------------------------------
void test_decoder() {
    Decoder decoder;

    // ทดสอบ R-type: add 1 2 3 -> opcode=0, regA=1, regB=2, destReg=3
    // (0 << 22) | (1 << 19) | (2 << 16) | 3 = 655363
    int addCode = (0 << 22) | (1 << 19) | (2 << 16) | 3;
    Instruction instAdd = decoder.decode(addCode);
    assert(instAdd.opcode == 0);
    assert(instAdd.regA == 1);
    assert(instAdd.regB == 2);
    assert(instAdd.destReg == 3);

    // ทดสอบ I-type Offset ติดลบ: beq 0 0 -1 (offset 16-bit = 0xFFFF)
    // (4 << 22) | (0 << 19) | (0 << 16) | 0xFFFF
    int beqNegCode = (4 << 22) | (0 << 19) | (0 << 16) | 0xFFFF;
    Instruction instBeq = decoder.decode(beqNegCode);
    assert(instBeq.opcode == 4);
    assert(instBeq.offset == -1 && "Decoder failed to sign-extend negative offset -1");

    std::cout << "[PASS] Test 1: Decoder Bitfields & Sign Extension\n";
}

// -------------------------------------------------------------
// Test 2: ทดสอบ Hardwired Zero (registers[0] ต้องเป็น 0 เสมอ)
// -------------------------------------------------------------
void test_hardwired_zero() {
    stateStruct state;
    Executor executor;

    state.registers[1] = 50;
    state.registers[2] = 50;

    // คำสั่ง: add 1 2 0 (พยายามเขียน 100 ลง registers[0])
    Instruction inst;
    inst.opcode = 0;
    inst.regA = 1;
    inst.regB = 2;
    inst.destReg = 0;

    bool cont = executor.execute(inst, state);
    assert(cont == true);
    assert(state.registers[0] == 0 && "Critical: registers[0] must strictly remain 0!");

    std::cout << "[PASS] Test 2: Hardwired Zero Invariant (registers[0] == 0)\n";
}

// -------------------------------------------------------------
// Test 3: ทดสอบ Memory Read/Write Bounds และค่าข้อมูล (lw / sw)
// -------------------------------------------------------------
void test_memory_operations() {
    stateStruct state;
    Executor executor;

    state.registers[1] = 10; // Base address
    state.registers[2] = 999; // Data to store

    // sw 1 2 5 -> mem[10 + 5] = 999
    Instruction swInst{3, 1, 2, 0, 5};
    bool swOk = executor.execute(swInst, state);
    assert(swOk == true);
    assert(state.memory[15] == 999 && "sw failed to write correct value");

    // lw 1 3 5 -> registers[3] = mem[10 + 5] (999)
    Instruction lwInst{2, 1, 3, 0, 5};
    bool lwOk = executor.execute(lwInst, state);
    assert(lwOk == true);
    assert(state.registers[3] == 999 && "lw failed to load stored value");

    // ทดสอบ Memory Bounds Check: เข้าถึง Memory ติดลบ
    Instruction outOfBoundsInst{2, 0, 4, 0, -10}; // address = 0 + (-10) = -10
    bool oobOk = executor.execute(outOfBoundsInst, state);
    assert(oobOk == false && "Executor should reject negative memory address");

    std::cout << "[PASS] Test 3: Memory Access & Bounds Checking (lw/sw)\n";
}

// -------------------------------------------------------------
// Test 4: ทดสอบการแตกกิ่ง (beq Forward, Backward, และ Not Taken)
// -------------------------------------------------------------
void test_branch_conditions() {
    stateStruct state;
    Executor executor;

    state.pc = 5;
    state.registers[1] = 10;
    state.registers[2] = 10;
    state.registers[3] = 20;

    // beq 1 3 2 (Not Taken: 10 != 20) -> pc ควรเป็น pc + 1 = 6
    Instruction beqNotTaken{4, 1, 3, 0, 2};
    executor.execute(beqNotTaken, state);
    assert(state.pc == 6 && "beq Not Taken failed (pc should be pc + 1)");

    // beq 1 2 3 (Taken Forward: 10 == 10) -> pc = 6 + 3 + 1 = 10
    Instruction beqTakenFwd{4, 1, 2, 0, 3};
    executor.execute(beqTakenFwd, state);
    assert(state.pc == 10 && "beq Forward Taken failed (pc += offset + 1)");

    // beq 1 2 -3 (Taken Backward: 10 == 10) -> pc = 10 + (-3) + 1 = 8
    Instruction beqTakenBkwd{4, 1, 2, 0, -3};
    executor.execute(beqTakenBkwd, state);
    assert(state.pc == 8 && "beq Backward Taken failed");

    std::cout << "[PASS] Test 4: Branch Logic (Forward, Backward, Not Taken)\n";
}

// -------------------------------------------------------------
// Test 5: ทดสอบ JALR และ Edge Case (regA == regB)
// -------------------------------------------------------------
void test_jalr_linkage() {
    stateStruct state;
    Executor executor;

    // Case 5.1: jalr ปกติ (regA != regB)
    state.pc = 3;
    state.registers[1] = 25; // Target PC
    Instruction jalrInst{5, 1, 2, 0, 0}; // jalr 1 2
    executor.execute(jalrInst, state);
    assert(state.registers[2] == 4 && "registers[regB] must store pc + 1");
    assert(state.pc == 25 && "pc must jump to registers[regA]");

    // Case 5.2: jalr กรณี regA == regB เช่น jalr 3 3
    state.pc = 10;
    state.registers[3] = 40; // Target PC
    Instruction jalrSameReg{5, 3, 3, 0, 0}; // jalr 3 3
    executor.execute(jalrSameReg, state);
    assert(state.pc == 40 && "JALR must jump to original target address");
    assert(state.registers[3] == 11 && "JALR link register must be updated to pc + 1");

    std::cout << "[PASS] Test 5: Subroutine Linkage (jalr & edge cases)\n";
}

// -------------------------------------------------------------
// Test 6: ทดสอบ Halt และ Noop
// -------------------------------------------------------------
void test_halt_and_noop() {
    stateStruct state;
    Executor executor;

    state.pc = 0;
    // noop
    Instruction noopInst{7, 0, 0, 0, 0};
    bool contNoop = executor.execute(noopInst, state);
    assert(contNoop == true && "noop should allow execution to continue");
    assert(state.pc == 1 && "noop should increment pc");

    // halt
    Instruction haltInst{6, 0, 0, 0, 0};
    bool contHalt = executor.execute(haltInst, state);
    assert(contHalt == false && "halt must return false to stop simulation");
    assert(state.pc == 2 && "halt should increment pc before stopping");

    std::cout << "[PASS] Test 6: Halt and Noop Operations\n";
}

// -------------------------------------------------------------
// Test: ทดสอบคำสั่ง nand (Logic, Bitwise Inversion, reg[0])
// -------------------------------------------------------------
void test_nand_operations() {
    stateStruct state;
    Executor executor;

    // --- Case 1: Bitwise NAND ทั่วไป ---
    // กำหนดค่าทดสอบ
    // reg[1] = 12 (0000 1100_2)
    // reg[2] = 10 (0000 1010_2)
    // 12 & 10 = 8 (0000 1000_2)
    // ~(12 & 10) = ~8 = -9 ในระบบ Two's Complement 32 บิต
    state.registers[1] = 12;
    state.registers[2] = 10;

    // คำสั่ง: nand 1 2 3 -> registers[3] = ~(registers[1] & registers[2])
    Instruction nandInst{1, 1, 2, 3, 0};
    bool ok1 = executor.execute(nandInst, state);

    assert(ok1 == true);
    assert(state.registers[3] == ~(12 & 10) && "nand operation failed for general values");
    assert(state.registers[3] == -9);

    // --- Case 2: Bitwise NOT (X NAND X == ~X) ---
    // ใช้กลับบิตค่า 37
    // ~37 ในระบบ Two's Complement จะได้ -38
    state.registers[4] = 37;
    Instruction notInst{1, 4, 4, 5, 0}; // nand 4 4 5 -> registers[5] = ~registers[4]
    bool ok2 = executor.execute(notInst, state);

    assert(ok2 == true);
    assert(state.registers[5] == -38 && "nand failed to act as bitwise NOT");

    // --- Case 3: Hardwired Zero สำหรับคำสั่ง nand ---
    // ทดสอบสั่งให้ปลายทาง destReg เป็น registers[0]
    Instruction zeroInst{1, 1, 2, 0, 0}; // nand 1 2 0
    bool ok3 = executor.execute(zeroInst, state);

    assert(ok3 == true);
    assert(state.registers[0] == 0 && "nand must not overwrite registers[0]");

    std::cout << "[PASS] Test: NAND Logic, Bitwise Inversion (~X), and reg[0] Invariance\n";
}

int main() {
    std::cout << "=========================================\n";
    std::cout << " Running LC-2K Simulator Unit Tests...\n";
    std::cout << "=========================================\n";

    test_decoder();
    test_hardwired_zero();
    test_nand_operations();
    test_memory_operations();
    test_branch_conditions();
    test_jalr_linkage();
    test_halt_and_noop();

    std::cout << "=========================================\n";
    std::cout << " >>> ALL UNIT TESTS PASSED (7/7) <<<    \n";
    std::cout << "=========================================\n";
    return 0;
}