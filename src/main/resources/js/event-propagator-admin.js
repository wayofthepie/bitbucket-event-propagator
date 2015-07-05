AJS.toInit(function() {
  console.log(AJS.params.baseURL)
  function populateForm() {
    AJS.$.ajax({
      url: AJS.contextPath() + "/rest/event-propagator/1.0/",
      dataType: "json",
      success: function(config) {
        AJS.$("#name").attr("value", config.name);
        AJS.$("#time").attr("value", config.time);
      }
    });
  }
  function updateConfig() {
    AJS.$.ajax({
      url: AJS.contextPath() + "/rest/event-propagator/1.0/",
      type: "PUT",
      contentType: "application/json",
      data: '{ "name": "' + AJS.$("#name").attr("value") + '", "time": "' +  AJS.$("#time").attr("value") + '" }',
      processData: false,
      success: function(response, status, xhr) {
        console.log(xhr.status)
      },
      complete: function(xhr, textStatus) {
        console.log(xhr.status);
      }
    });
  }
  populateForm();

  AJS.$("#admin").submit(function(e) {
    e.preventDefault();
    updateConfig();
  });
});