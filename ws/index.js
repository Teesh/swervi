const { WebSocketServer } =require('ws');
const wss = new WebSocketServer({ port: 3300 });
var wsConnected = 0;
var thisws;

//const port = new SerialPort({ autoOpen:false, path: '/dev/cu.ESP32Bluetooth', baudRate: 115200 })

var btSerial = new (require("bluetooth-serial-port").BluetoothSerialPort)();
var btConnected = 0;
var thisbt;
var lastX = 0, lastY = 0, lastZ = 0;

// Mediate the messages that come from the ESP32 and got to the Simultor
// Messages from the ESP32 look like this 
//     {"type": "Encoder", "period": 0.01, encoders:[0,0,0,0,0,0,0,0]}
// The message to the simulator look like this 
//     {"type": "Encoder", "device": "1", "data":{"<channel_a": 2, "<channel_b": 3, ">count": 0,  ">period": 1.000000 }}
function handleSwervi2Sim(buffer) {
	
	var message = buffer.toString("utf-8").trim();
//	console.log("Unparsed:" + message + ":End");
	
	if (message.length < 3) {
		console.log("Message too small");
		return;
	}
	
	if (message.length > 136) {
		console.log("Message too large");
		return;
	}
	
	if (message.charAt(0) !== '{') {
		console.log("Not valid JSON");
		return;
	}
	
	//console.log("Message OK");
	
	
	var jsonData = JSON.parse(buffer);
//	console.log("Parsed:" + JSON.stringify(jsonData));
	
	for (var c = 0; c < 8; c++) {
		
		// Build outgoing websocket message for encoder[c]
		var data = {};
		data["<channel_a"] = String(c*2);
		data["<channel_b"] = String(c*2 + 1);
		data[">period"] = jsonData.period;
		data[">count"] = jsonData.encoders[c];
	    var outMessage = {"type": "Encoder", "device": String(c), "data": data};
	
        //console.log("outMessage:" + JSON.stringify(outMessage));
	
	    if (wsConnected) {
			thisws.send(JSON.stringify(outMessage));
	    }
	}
	const loopTime = 0.04;
	
	var rateX, rateY, rateZ;
	rateX = (jsonData.encoders[8] - lastX) / loopTime;
	rateY = (jsonData.encoders[9] - lastY) / loopTime;
	rateZ = (jsonData.encoders[10] - lastZ) / loopTime;

	lastX = jsonData.encoders[8];
	lastY = jsonData.encoders[9];
	lastZ = jsonData.encoders[10];

	// console.log("Gyro X:" + jsonData.encoders[8] + " Y:" + jsonData.encoders[9] + " Z:" + jsonData.encoders[10]);
	// console.log("Rates: Gyro X:" + rateX + " Y:" + rateY + " Z:" + rateZ);
	// console.log("Rates: Gyro X:" + lastX + " Y:" + lastY + " Z:" + lastZ);
	var outMessage = {
		"data": {
			">angle_x": jsonData.encoders[8],
			">angle_y": jsonData.encoders[9],
			">angle_z": jsonData.encoders[10],
			">rate_x": rateX,
			">rate_y": rateY,
			">rate_z": rateZ,
		},
		"device": "SwerviGyro",
		"type": "Gyro"
	};
	if (wsConnected) {
		thisws.send(JSON.stringify(outMessage));
	}

}

var motors = [0,0,0,0,0,0,0,0];
setInterval(function(){
	if (btConnected) {
//console.log(JSON.stringify(motors));
		
		thisbt.write(Buffer.from(JSON.stringify(motors) + '\0', 'utf-8'), function (err, bytesWritten) {
            if (err) console.log(err);
        });
	}
}, 20);  // Every few ms

function handleSim2Swervi(data) {
	const jsonData = JSON.parse(data);

	if (jsonData.type !== 'PWM') return;
//console.log(JSON.stringify(jsonData));	

	var motor = Number(jsonData.device);
	
	if (motor < 8) {
	  var speed = Number(jsonData.data["<speed"]);
	  if (speed % 2 !== 0) {
		  speed = (Math.round(speed * 100)) / 100;
	  }

      motors[motor] = speed;	
	}
}

function connect() {
  btSerial.listPairedDevices(function (list) {
	
    var deviceList = JSON.stringify(list,null,2);
	// console.log(deviceList);
		
	list.forEach(function(element) {
		//console.log("FE:" + JSON.stringify(element));
		if (element.name === "Swervi") {
            console.log("Found SWERVI!!!");
			
			console.log("Wait for Robot Simulator");
            wss.on('connection', function connection(ws) {
	            wsConnected = 1;
				thisws = ws;
            	console.log("Connected to Simulator");
				
				btSerial.connect(element.address, element.services[0].channel, function () {
                    btConnected = 1;
	  			    thisbt = btSerial;
		 	        console.log("connected to Swervi");

				    // This is a test
				    //btSerial.write(Buffer.from("Hi Swervi, I'm Sim", "utf-8"), function (err, bytesWritten) {
                    //    if (err) console.log(err);
                    //});
                   btSerial.on("data", handleSwervi2Sim);
				}, function () {
					console.log("cannot connect");
				});	
				ws.on('message', handleSim2Swervi);
            });
		}
	});
  });
}

console.log("Starting SwerviWS");
connect();
