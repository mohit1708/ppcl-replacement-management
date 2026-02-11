<!-- Edit Commercial Modal -->
<div class="modal fade" id="commercialModal" tabindex="-1" role="dialog" aria-labelledby="commercialModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content border-0 radius-1 shadow">
            <div class="modal-header bgc-success-d1 text-white py-2 pl-3">
                <h5 class="modal-title text-110" id="commercialModalLabel">
                    <i class="fas fa-edit mr-1"></i> Edit Commercial Details
                </h5>
                <button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body p-4">
                <div class="form-group mb-3">
                    <label class="text-600 text-90">Printer Serial</label>
                    <input type="text" class="form-control brc-on-focus brc-success-m1" id="commPrinterSerial" disabled>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group mb-3">
                            <label class="text-600 text-90">Existing Cost</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text bgc-white brc-grey-l1 text-success-d1">₹</span>
                                </div>
                                <input type="number" class="form-control" id="commExistingCost" step="0.01" disabled>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group mb-3">
                            <label class="text-600 text-90">New Cost <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text bgc-white brc-success-m3 text-success">₹</span>
                                </div>
                                <input type="number" class="form-control brc-on-focus brc-success-m1" id="commNewCost" step="0.01" placeholder="0.00">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group mb-3">
                            <label class="text-600 text-90">Existing Rental</label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text bgc-white brc-grey-l1 text-success-d1">₹</span>
                                </div>
                                <input type="number" class="form-control" id="commExistingRental" step="0.01" disabled>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group mb-3">
                            <label class="text-600 text-90">New Rental <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <div class="input-group-prepend">
                                    <span class="input-group-text bgc-white brc-success-m3 text-success">₹</span>
                                </div>
                                <input type="number" class="form-control brc-on-focus brc-success-m1" id="commNewRental" step="0.01" placeholder="0.00">
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group mb-0">
                    <label class="text-600 text-90">Justification <span class="text-danger">*</span></label>
                    <textarea class="form-control brc-on-focus brc-success-m1" id="commJustification" rows="3" 
                              placeholder="Explain the commercial changes and rationale"></textarea>
                </div>

                <div class="alert bgc-success-l4 border-none border-l-4 brc-success-m2 mt-4 mb-0 py-2">
                    <i class="fas fa-info-circle text-success-m1 mr-2"></i>
                    <span class="text-success-d2 text-90">Fill in the <strong>New Cost</strong> and <strong>New Rental</strong> fields with proposed commercial terms.</span>
                </div>
            </div>
            <div class="modal-footer bgc-grey-l4 border-t-1 brc-grey-l3">
                <button type="button" class="btn btn-outline-default btn-bgc-white btn-bold radius-1" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-success btn-bold px-4 radius-1" onclick="submitCommercial()">
                    <i class="fas fa-save mr-1"></i> Save Commercial
                </button>
            </div>
        </div>
    </div>
</div>
