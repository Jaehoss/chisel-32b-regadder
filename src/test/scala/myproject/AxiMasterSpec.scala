package myproject

import chisel3._
import chisel3.util._
import chisel3.simulator.ChiselSim
import org.scalatest.flatspec._
import svsim._

class AxiMasterSpec extends AnyFlatSpec with ChiselSim {
  "AxiMaster" should "perform a single write and then a single read" in {
    simulate(new AxiMaster(ADDR_WIDTH = 32, DATA_WIDTH = 32)) { dut =>
      // Initialize all inputs
      dut.io.start_write.poke(false.B)
      dut.io.write_addr.poke(0.U)
      dut.io.write_data.poke(0.U)
      dut.io.start_read.poke(false.B)
      dut.io.read_addr.poke(0.U)

        // Initialize AXI slave-side inputs (master's outputs are slave's inputs)
        dut.io.axi.awready.poke(false.B)
        dut.io.axi.wready.poke(false.B)
        dut.io.axi.bvalid.poke(false.B)
        dut.io.axi.bid.poke(0.U)
        dut.io.axi.bresp.poke(0.U)

        dut.io.axi.arready.poke(false.B)
      dut.io.axi.rvalid.poke(false.B)
      dut.io.axi.rlast.poke(false.B)
        dut.io.axi.rid.poke(0.U)
        dut.io.axi.rdata.poke(0.U)
        dut.io.axi.rresp.poke(0.U)

        dut.clock.step(10) // Wait some cycles for reset/initialization

        val testAddr = "h1000".U(32.W)
        val testData = "hDEADBEEF".U(32.W)

        println("\n--- Starting Write Test ---")
      // Start Write Operation
        dut.io.start_write.poke(true.B)
        dut.io.write_addr.poke(testAddr)
        dut.io.write_data.poke(testData)
      dut.clock.step(1)
      dut.io.start_write.poke(false.B) // De-assert start signal

        // Wait for AWVALID and assert AWREADY
      while (!dut.io.axi.awvalid.peek().litToBoolean) {
          dut.clock.step(1)
    }
        dut.io.axi.awready.poke(true.B)
        println(s"AWADDR: ${dut.io.axi.awaddr.peek().toString()}")
        dut.io.axi.awaddr.expect(testAddr)
        dut.clock.step(1)
        dut.io.axi.awready.poke(false.B)

        // Wait for WVALID and assert WREADY
      while (!dut.io.axi.wvalid.peek().litToBoolean) {
          dut.clock.step(1)
  }
        dut.io.axi.wready.poke(true.B)
        println(s"WDATA: ${dut.io.axi.wdata.peek().toString()}")
        dut.io.axi.wdata.expect(testData)
        dut.clock.step(1)
        dut.io.axi.wready.poke(false.B)

        // Wait for BREADY from master, then assert BVALID
      while (!dut.io.axi.bready.peek().litToBoolean) {
          dut.clock.step(1)
}
      dut.io.axi.bvalid.poke(true.B)
      dut.io.axi.bresp.poke(0.U) // OKAY
        dut.clock.step(1) // Handshake occurs here
        dut.io.axi.bvalid.poke(false.B)
        dut.io.write_done.expect(true.B)
        println("Write operation completed.")
        dut.clock.step(1)
      dut.io.write_done.expect(false.B) // write_done should de-assert

      dut.clock.step(10) // Some idle cycles

        println("\n--- Starting Read Test ---")
        // Start Read Operation
        dut.io.start_read.poke(true.B)
        dut.io.read_addr.poke(testAddr)
        dut.clock.step(1)
      dut.io.start_read.poke(false.B) // De-assert start signal

        // Wait for ARVALID and assert ARREADY
      while (!dut.io.axi.arvalid.peek().litToBoolean) {
          dut.clock.step(1)
        }
      dut.io.axi.arready.poke(true.B)
      println(s"ARADDR: ${dut.io.axi.araddr.peek().toString()}")
      dut.io.axi.araddr.expect(testAddr)
          dut.clock.step(1)
      dut.io.axi.arready.poke(false.B)

      // Wait for RREADY from master, then assert RVALID and RLAST, provide read data
      val readBackData = "hCAFEF00D".U(32.W)
      while (!dut.io.axi.rready.peek().litToBoolean) {
        dut.clock.step(1)
    }
      dut.io.axi.rvalid.poke(true.B)
      dut.io.axi.rlast.poke(true.B)
      dut.io.axi.rdata.poke(readBackData)
      dut.io.axi.rresp.poke(0.U) // OKAY

      // EXPECTATION MOVED HERE: io.read_data should be updated combinatorially
      println(s"READ DATA from Master: ${dut.io.read_data.peek().toString()}")
      dut.io.read_data.expect(readBackData) // Check before clock step

      dut.clock.step(1) // Handshake occurs here, master transitions state
      dut.io.axi.rvalid.poke(false.B)
      dut.io.axi.rlast.poke(false.B)

      dut.io.read_done.expect(true.B)
      println("Read operation completed.")
      dut.clock.step(1)
      dut.io.read_done.expect(false.B) // read_done should de-assert

      dut.clock.step(10) // Final idle cycles
    }
  }
}
