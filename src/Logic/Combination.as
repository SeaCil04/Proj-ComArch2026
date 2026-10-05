lw      0       5       pos1      ; เริ่มตัวชี้ Stack ที่ 1
        lw      0       1       n
        lw      0       2       r
        lw      0       6       caddr
        jalr    6       7
        halt
comBi   lw      0       6       pos1      ; โหลด 1 สำหรับเพิ่มตัวชี้ Stack
        sw      5       7       stack     ; เก็บตำแหน่งกลับลง Stack
        add     5       6       5
        sw      5       1       stack     ; เก็บค่า n ลง Stack
        add     5       6       5
        sw      5       2       stack     ; เก็บค่า r ลง Stack
        add     5       6       5
        lw      0       4       zero
        beq     2       4       base
        beq     1       2       base
        lw      0       4       neg1
        add     1       4       1         ; ลด n ลง 1
        lw      0       6       caddr
        jalr    6       7                 ; เรียกหาค่า C(n-1, r)
        lw      0       6       pos1
        sw      5       3       stack     ; เก็บผลการเรียกครั้งแรกลง Stack
        add     5       6       5
        lw      0       4       neg1
        add     2       4       2         ; ลด r ลง 1
        lw      0       6       caddr
        jalr    6       7                 ; เรียกหาค่า C(n-1, r-1)
        lw      0       6       neg1      ; โหลด -1 สำหรับลดตัวชี้ Stack
        add     5       6       5
        lw      5       4       stack     ; ดึงผลการเรียกครั้งแรกจาก Stack
        add     3       4       3         ; รวมผลทั้งสองครั้งไว้ใน reg3
        add     5       6       5
        lw      5       2       stack     ; คืนค่า r เดิม
        add     5       6       5
        lw      5       1       stack     ; คืนค่า n เดิม
        add     5       6       5
        lw      5       7       stack     ; คืนตำแหน่งกลับของผู้เรียก
        jalr    7       6
base    lw      0       3       pos1      ; กรณีฐานให้คำตอบเป็น 1
        lw      0       6       neg1
        add     5       6       5
        lw      5       2       stack
        add     5       6       5
        lw      5       1       stack
        add     5       6       5
        lw      5       7       stack
        jalr    7       6
pos1    .fill   1
neg1    .fill   -1
zero    .fill   0
caddr   .fill   comBi
n       .fill   5
r       .fill   2
stack   .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0