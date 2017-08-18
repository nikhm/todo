function displayInfo(result){
    var elem = document.getElementById("info");
    elem.innerHTML = result;
    console.log("Toggling the display element.");
    console.log(result);
    if(result != "") toggleDisplay("infoContain");
}

function toggleDisplay(id) {
    var e = document.getElementById(id);
    if(e.style.display == "block") {
        e.style.display = "none";
    } else {
        e.style.display = "block";
    }
}