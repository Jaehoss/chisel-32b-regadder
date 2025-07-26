package myproject

import chisel3._
import chisel3.util._

// AXI4 Bundle definition for a Master interface
class Axi4MasterBundle(val ADDR_WIDTH: Int, val DATA_WIDTH: Int, val ID_WIDTH: Int = 1) extends Bundle {
  // AW channel (Write Address)
  val awid    = Output(UInt(ID_WIDTH.W))
  val awaddr  = Output(UInt(ADDR_WIDTH.W))
  val awlen   = Output(UInt(8.W))
  val awsize  = Output(UInt(3.W))
  val awburst = Output(UInt(2.W))
  val awlock  = Output(Bool())
  val awcache = Output(UInt(4.W))
  val awprot  = Output(UInt(3.W))
  val awqos   = Output(UInt(4.W))
  val awregion= Output(UInt(4.W))
  val awuser  = Output(UInt(0.W)) // Assuming 0-width for now
  val awvalid = Output(Bool())
  val awready = Input(Bool())

  // W channel (Write Data)
  val wdata   = Output(UInt(DATA_WIDTH.W))
  val wstrb   = Output(UInt((DATA_WIDTH/8).W))
  val wlast   = Output(Bool())
  val wuser   = Output(UInt(0.W)) // Assuming 0-width for now
  val wvalid  = Output(Bool())
  val wready  = Input(Bool())

  // B channel (Write Response)
  val bid     = Input(UInt(ID_WIDTH.W))
  val bresp   = Input(UInt(2.W))
  val buser   = Input(UInt(0.W)) // Assuming 0-width for now
  val bvalid  = Input(Bool())
  val bready  = Output(Bool())

  // AR channel (Read Address)
  val arid    = Output(UInt(ID_WIDTH.W))
  val araddr  = Output(UInt(ADDR_WIDTH.W))
  val arlen   = Output(UInt(8.W))
  val arsize  = Output(UInt(3.W))
  val arburst = Output(UInt(2.W))
  val arlock  = Output(Bool())
  val arcache = Output(UInt(4.W))
  val arprot  = Output(UInt(3.W))
  val arqos   = Output(UInt(4.W))
  val arregion= Output(UInt(4.W))
  val aruser  = Output(UInt(0.W)) // Assuming 0-width for now
  val arvalid = Output(Bool())
  val arready = Input(Bool())

  // R channel (Read Data)
  val rid     = Input(UInt(ID_WIDTH.W))
  val rdata   = Input(UInt(DATA_WIDTH.W))
  val rresp   = Input(UInt(2.W))
  val rlast   = Input(Bool())
  val ruser   = Input(UInt(0.W)) // Assuming 0-width for now
  val rvalid  = Input(Bool())
  val rready  = Output(Bool())
}

// AXI4 Master Module
class AxiMaster(val ADDR_WIDTH: Int, val DATA_WIDTH: Int, val ID_WIDTH: Int = 1) extends Module {
  val io = IO(new Bundle {
    val axi = new Axi4MasterBundle(ADDR_WIDTH, DATA_WIDTH, ID_WIDTH)
    // Control signals for demonstration
    val start_write = Input(Bool())
    val write_addr  = Input(UInt(ADDR_WIDTH.W))
    val write_data  = Input(UInt(DATA_WIDTH.W))
    val write_done  = Output(Bool())

    val start_read  = Input(Bool())
    val read_addr   = Input(UInt(ADDR_WIDTH.W))
    val read_data   = Output(UInt(DATA_WIDTH.W))
    val read_done   = Output(Bool())
  })

  // Initialize AXI outputs to default values
  io.axi.awid     := 0.U
  io.axi.awaddr   := 0.U
  io.axi.awlen    := 0.U // Single beat
  io.axi.awsize   := (log2Ceil(DATA_WIDTH/8)).U
  io.axi.awburst  := 1.U  // INCR
  io.axi.awlock   := false.B
  io.axi.awcache  := "b0011".U
  io.axi.awprot   := "b000".U
  io.axi.awqos    := "b0000".U
  io.axi.awregion := "b0000".U
  io.axi.awuser   := 0.U
  io.axi.awvalid  := false.B

  io.axi.wdata    := 0.U
  io.axi.wstrb    := (~0.U((DATA_WIDTH/8).W)) // All bytes enabled
  io.axi.wlast    := true.B
  io.axi.wuser    := 0.U
  io.axi.wvalid   := false.B

  io.axi.arid     := 0.U
  io.axi.araddr   := 0.U
  io.axi.arlen    := 0.U // Single beat
  io.axi.arsize   := (log2Ceil(DATA_WIDTH/8)).U
  io.axi.arburst  := 1.U  // INCR
  io.axi.arlock   := false.B
  io.axi.arcache  := "b0011".U
  io.axi.arprot   := "b000".U
  io.axi.arqos    := "b0000".U
  io.axi.arregion := "b0000".U
  io.axi.aruser   := 0.U
  io.axi.arvalid  := false.B

  val write_done_reg = RegInit(false.B)
  val read_done_reg  = RegInit(false.B)

  io.write_done := write_done_reg
  io.read_done  := read_done_reg

  // Combinational wire for read_data output
  val read_data_wire = Wire(UInt(DATA_WIDTH.W))
  read_data_wire := 0.U // Default value for the wire, overridden by FSM

  // Write FSM
  val sWriteIdle :: sWriteAddr :: sWriteData :: sWriteResp :: Nil = Enum(4)
  val writeState = RegInit(sWriteIdle)

  // Write channel registers
  val awaddr_reg  = RegInit(0.U(ADDR_WIDTH.W))
  val wdata_reg   = RegInit(0.U(DATA_WIDTH.W))

  // AXI AW and W valid signals are driven by FSM
  // AXI Bready is controlled here to ensure it's always driven after writeState is initialized
  when (writeState === sWriteResp) {
    io.axi.bready := true.B
  } .otherwise {
    io.axi.bready := false.B
  }

  switch (writeState) {
    is (sWriteIdle) {
      write_done_reg := false.B // De-assert when idle
      when (io.start_write) {
        awaddr_reg  := io.write_addr
        wdata_reg   := io.write_data
        io.axi.awvalid := true.B
        writeState  := sWriteAddr
      }
    }
    is (sWriteAddr) {
      io.axi.awvalid := true.B
      when (io.axi.awready) {
        io.axi.awvalid := false.B
        io.axi.wvalid  := true.B
        writeState := sWriteData
      }
    }
    is (sWriteData) {
      io.axi.wvalid := true.B
      when (io.axi.wready) {
        io.axi.wvalid := false.B
        writeState := sWriteResp
      }
    }
    is (sWriteResp) {
      // Handshake: both bvalid and bready must be true for transaction completion
      when (io.axi.bvalid && io.axi.bready) {
        write_done_reg := true.B // Assert for one cycle
        writeState := sWriteIdle
      }
    }
  }

  // Read FSM
  val sReadIdle :: sReadAddr :: sReadData :: Nil = Enum(3)
  val readState = RegInit(sReadIdle)

  // Read channel registers
  val araddr_reg  = RegInit(0.U(ADDR_WIDTH.W))

  // AXI AR valid signal is driven by FSM
  // AXI Rready is controlled here to ensure it's always driven after readState is initialized
  when (readState === sReadData) {
    io.axi.rready := true.B
  } .otherwise {
    io.axi.rready := false.B
  }
  switch (readState) {
    is (sReadIdle) {
      read_done_reg := false.B // De-assert when idle
      read_data_wire := 0.U // Assign to the wire
      when (io.start_read) {
        araddr_reg  := io.read_addr
        io.axi.arvalid := true.B
        readState := sReadAddr
      }
    }
    is (sReadAddr) {
      io.axi.arvalid := true.B
      read_data_wire := 0.U // Assign to the wire during address phase
      when (io.axi.arready) {
        io.axi.arvalid := false.B
        readState := sReadData
      }
    }
    is (sReadData) {
      // Handshake: rvalid, rlast, and rready must be true for a single beat transfer
      when (io.axi.rvalid && io.axi.rlast && io.axi.rready) {
        read_data_wire := io.axi.rdata // Assign actual read data to the wire
        read_done_reg := true.B // Assert for one cycle
        readState := sReadIdle
      } .otherwise {
        read_data_wire := 0.U // Ensure assigned if handshake not met
      }
    }
  }

  // Connect the wire to the output port
  io.read_data := read_data_wire

  // Connect registers to AXI outputs
  io.axi.awaddr := awaddr_reg
  io.axi.wdata  := wdata_reg
  io.axi.araddr := araddr_reg
}
