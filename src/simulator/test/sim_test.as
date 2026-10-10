        lw      0       1       valA            ! 0: r1 = 12
        lw      0       2       valB            ! 1: r2 = 10
        add     1       2       0               ! 2: Test reg[0] invariance (reg[0] ต้องเป็น 0 เสมอ)
        nand    1       2       3               ! 3: r3 = ~(12 & 10) = -9
        nand    1       1       4               ! 4: r4 = ~12 = -13 (Bitwise NOT)
        sw      0       3       memScr          ! 5: บันทึก r3 (-9) ลง memory
        lw      0       5       memScr          ! 6: r5 = โหลดค่าจาก memory (-9)
        beq     1       2       errBra          ! 7: 12 != 10 ต้องไม่กระโดด
        beq     3       5       passFw         ! 8: -9 == -9 กระโดดข้ามไปข้างหน้า
errBra halt                                     ! 9: ดักจับกรณี Branch ผิดพลาด
passFw lw      0       6       fnTar            ! 10: r6 = โหลด target address (13)
        jalr    6       7                       ! 11: กระโดดไป fnTarget, r7 = PC + 1 = 12
retPt   halt                                    ! 12: จุดหยุดโปรแกรมหลัก
subFn   noop                                    ! 13: ทดสอบ noop
        jalr    7       6                       ! 14: กระโดดกลับมาที่ retPt (Address 12)
valA    .fill   12                              ! 15
valB    .fill   10                              ! 16
memScr .fill 0                                  ! 17
fnTar .fill  13                                 ! 18