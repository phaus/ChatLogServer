jQuery(function($) {
	// bind detectLines Function to all LineNumberLinks.
	$('[data-link="line-link"]').click(function(event){
		detectLines(event.target.id);
	});
	detectLines();
});


var detectLines = function(line) {
	$('.entry').removeClass("highlight");
	var parts = document.URL.split("#");
	if(parts.length > 1 && parts[1].indexOf('L') === 0) {
		line = line || parts[1]; 
		var lines = line.substring(1,parts[1].length).trim().split("-");
		if(lines.length == 1) {
			$('[data-line="'+lines[0]+'"]').addClass("highlight");
		} else {
			var start = parseInt(lines[0]);
			var end = parseInt(lines[1]);
			for(i = start; i <= end; i++){
				$('[data-line="'+i+'"]').addClass("highlight");
			}
		}		
	}
};