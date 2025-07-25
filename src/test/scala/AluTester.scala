import chisel3._
import chisel3.util._
import chisel3.simulator.ChiselSim
import org.scalatest.freespec._

class AluTester extends AnyFreeSpec with ChiselSim {
    simulate(new ALU) { dut =>
      // Test ADD
      dut.io.aluOp.poke(ALUOp.ADD)
      dut.io.srcA.poke(10.U)
      dut.io.srcB.poke(5.U)
      dut.clock.step(1)
      dut.io.result.expect(15.U)
      dut.io.zero.expect(false.B)

      // Test SUB
      dut.io.aluOp.poke(ALUOp.SUB)
      dut.io.srcA.poke(10.U)
      dut.io.srcB.poke(5.U)
      dut.clock.step(1)
      dut.io.result.expect(5.U)
      dut.io.zero.expect(false.B)

      // Test AND
      dut.io.aluOp.poke(ALUOp.AND)
      dut.io.srcA.poke(0xF0.U)
      dut.io.srcB.poke(0x0F.U)
      dut.clock.step(1)
      dut.io.result.expect(0.U)
      dut.io.zero.expect(true.B)

      // Test OR
      dut.io.aluOp.poke(ALUOp.OR)
      dut.io.srcA.poke(0xF0.U)
      dut.io.srcB.poke(0x0F.U)
      dut.clock.step(1)
      dut.io.result.expect(0xFF.U)
      dut.io.zero.expect(false.B)

      // Test XOR
      dut.io.aluOp.poke(ALUOp.XOR)
      dut.io.srcA.poke(0xFF.U)
      dut.io.srcB.poke(0x0F.U)
      dut.clock.step(1)
      dut.io.result.expect(0xF0.U)
      dut.io.zero.expect(false.B)

      // Test SLT (signed less than) - true case
      dut.io.aluOp.poke(ALUOp.SLT)
      // Fix: Explicitly specify 32-bit width for the signed literal
      val pokeSrcA = (-5).S(32.W).asUInt // Ensure -5 is represented as 32-bit 2's complement
      val pokeSrcB = 10.U
      dut.io.srcA.poke(pokeSrcA) // -5
      dut.io.srcB.poke(pokeSrcB)          // 10
      println(s"DEBUG Test: SLT True Case - Poking srcA=${pokeSrcA.litValue.toLong} (0x${pokeSrcA.litValue.toLong.toHexString}), srcB=${pokeSrcB.litValue.toLong}")
      dut.clock.step(1)
      println(s"DEBUG Test: SLT True Case - Peeked result=${dut.io.result.peek().litValue.toLong}, zero=${dut.io.zero.peek().litToBoolean}")
      dut.io.result.expect(1.U)
      dut.io.zero.expect(false.B)

      // Test SLT (signed less than) - false case
      dut.io.aluOp.poke(ALUOp.SLT)
      val pokeSrcA_false = 10.U
      // Fix: Explicitly specify 32-bit width for the signed literal
      val pokeSrcB_false = (-5).S(32.W).asUInt // Ensure -5 is represented as 32-bit 2's complement
      dut.io.srcA.poke(pokeSrcA_false)
      dut.io.srcB.poke(pokeSrcB_false)
      println(s"DEBUG Test: SLT False Case - Poking srcA=${pokeSrcA_false.litValue.toLong}, srcB=${pokeSrcB_false.litValue.toLong} (0x${pokeSrcB_false.litValue.toLong.toHexString})")
      dut.clock.step(1)
      println(s"DEBUG Test: SLT False Case - Peeked result=${dut.io.result.peek().litValue.toLong}, zero=${dut.io.zero.peek().litToBoolean}")
      dut.io.result.expect(0.U)
      dut.io.zero.expect(true.B)

      // Test SLL (shift left logical)
      dut.io.aluOp.poke(ALUOp.SLL)
      dut.io.srcA.poke(1.U)
      dut.io.srcB.poke(4.U) // Shift by 4
      dut.clock.step(1)
      dut.io.result.expect(16.U)
      dut.io.zero.expect(false.B)

      // Test SRL (shift right logical)
      dut.io.aluOp.poke(ALUOp.SRL)
      dut.io.srcA.poke(16.U)
      dut.io.srcB.poke(4.U) // Shift by 4
      dut.clock.step(1)
      dut.io.result.expect(1.U)
      dut.io.zero.expect(false.B)

      // Test SRA (shift right arithmetic) - positive number
      dut.io.aluOp.poke(ALUOp.SRA)
      dut.io.srcA.poke(16.U) // 16
      dut.io.srcB.poke(2.U) // Shift by 2
      dut.clock.step(1)
      dut.io.result.expect(4.U)
      dut.io.zero.expect(false.B)

      // Test SRA (shift right arithmetic) - negative number
      dut.io.aluOp.poke(ALUOp.SRA)
      dut.io.srcA.poke((-16).S.asUInt) // -16
      dut.io.srcB.poke(2.U) // Shift by 2
      dut.clock.step(1)
      dut.io.result.expect((-4).S.asUInt)
      dut.io.zero.expect(false.B)
    }
}
