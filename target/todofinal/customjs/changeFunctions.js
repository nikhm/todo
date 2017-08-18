function handleTodos(todos,numList){
    // Handles newly created todos
    currentList = numList;
    var l = todos.length;
    for(var i=0;i<l;i++){
        var todo = todos[i];
        allTodos[todo["category"]][todo["id"]] = todo ;
    }
}

function handleChanges(changes,finalPositions,changeUsers,numChanges){
    currentChange = numChanges;
    var l = changes.length;
    for(var i=0;i<l;i++){
        var todoId = parseInt(changes[i]);
        var finalPosition = (finalPositions[i]);
        var user = changeUsers[i];
        changeAllTodos(todoId,finalPosition,user);
    }
}

function changeAllTodos(todoId,finalPosition,changeUser){
    if(allTodos["new"][todoId] != null && allTodos["new"][todoId] != undefined){
        if(finalPosition == "delete"){
            allTodos["new"][todoId] = null;
            return;
        }
        var todo = allTodos["new"][todoId];
        todo["category"] = finalPosition;
        todo["userName"] = changeUser;
        allTodos["new"][todoId] = null;
        allTodos[finalPosition][todoId] = todo;
    }else if(allTodos["progress"][todoId] != null && allTodos["progress"][todoId] != undefined){
        if(finalPosition == "delete"){
            allTodos["progress"][todoId] = null;
            return;
        }
        var todo = allTodos["progress"][todoId];
        todo["category"] = finalPosition;
        todo["userName"] = changeUser;
        allTodos["progress"][todoId] = null;
        allTodos[finalPosition][todoId] = todo;
    }else{
        // Do nothing!
    }
}
