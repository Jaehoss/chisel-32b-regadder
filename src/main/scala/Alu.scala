import chisel3._
import chisel3.util._

// Define ALU operations
object ALUOp extends ChiselEnum {
  val ADD, SUB, AND, OR, XOR, SLT, SLL, SRL, SRA = Value
}

// Define ALU IO Bundle
class ALUIO extends Bundle {
  val srcA = Input(UInt(32.W))
  val srcB = Input(UInt(32.W))
  val aluOp = Input(ALUOp())
  val result = Output(UInt(32.W))
  val zero = Output(Bool())
}

// Define ALU Module
class ALU extends Module {
  val io = IO(new ALUIO)

  // Default values
  io.result := 0.U
  io.zero := false.B

  // Implement ALU operations
  switch(io.aluOp) {
    is(ALUOp.ADD) {
      io.result := io.srcA + io.srcB
    }
    is(ALUOp.SUB) {
      io.result := io.srcA - io.srcB
    }
    is(ALUOp.AND) {
      io.result := io.srcA & io.srcB
    }
    is(ALUOp.OR) {
      io.result := io.srcA | io.srcB
    }
    is(ALUOp.XOR) {
      io.result := io.srcA ^ io.srcB
    }
    is(ALUOp.SLT) { // Set Less Than (signed comparison)
      val signedA = io.srcA.asSInt
      val signedB = io.srcB.asSInt
      // Add this line for debugging:
      printf(p"SLT Debug: srcA_uint=${io.srcA} srcA_sint=${signedA} srcB_uint=${io.srcB} srcB_sint=${signedB}\n")
      io.result := Mux(signedA < signedB, 1.U, 0.U)
    }
    is(ALUOp.SLL) { // Shift Left Logical
      // Use the lower 5 bits of srcB as shift amount for 32-bit values
      io.result := io.srcA << io.srcB(4, 0)
    }
    is(ALUOp.SRL) { // Shift Right Logical
      // Use the lower 5 bits of srcB as shift amount
      io.result := io.srcA >> io.srcB(4, 0)
    }
    is(ALUOp.SRA) { // Shift Right Arithmetic (signed shift)
      // Cast to SInt for arithmetic shift, then back to UInt
      io.result := (io.srcA.asSInt >> io.srcB(4, 0)).asUInt
    }
  }

  // Calculate zero flag based on the final result
  io.zero := (io.result === 0.U)
}