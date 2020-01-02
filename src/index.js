import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {NativeModules, requireNativeComponent, View, findNodeHandle} from 'react-native';

const WpcButtonManager = NativeModules.Printer;
const CAMERA_REF = 'wpcBtn';


type DetectOption = {
    fname?: string
};

function convertNativeProps(props) {
    const newProps = {...props};
    if (typeof props.aspect === 'string') {
        newProps.aspect = WpcButtonClass.constants.Aspect[props.aspect];
    }
    if (typeof props.torchMode === 'string') {
        newProps.torchMode = WpcButtonClass.constants.TorchMode[props.torchMode];
    }
    if (typeof props.captureQuality === 'string') {
        newProps.captureQuality = WpcButtonClass.constants.CaptureQuality[props.captureQuality];
    }
    if (typeof props.cameraType === 'string') {
        newProps.cameraType = WpcButtonClass.constants.CameraType[props.cameraType];
    }
    if (typeof props.model === 'string') {
        newProps.model = WpcButtonClass.constants.Model[props.model];
    }
    if (typeof props.rotateMode === 'string') {
        newProps.rotateMode = WpcButtonClass.constants.RotateMode[props.rotateMode];
    }
    
    delete newProps.onTrained;
    delete newProps.onRecognized;
    delete newProps.onFaceCaptured;
    delete newProps.onWpcButtonClicked;
    delete newProps.onUntrained;
    delete newProps.onUnrecognized;
    
    return newProps;
}

export default class WpcButtonClass extends Component {
    
    static constants = {
        Aspect: WpcButtonManager.Aspect,
        CaptureQuality: WpcButtonManager.CaptureQuality,
        TorchMode: WpcButtonManager.TorchMode,
        CameraType: WpcButtonManager.CameraType,
        Model: WpcButtonManager.Model,
        RotateMode: WpcButtonManager.RotateMode,
    };

    static propTypes = {
        ...View.propTypes,
        aspect: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        captureQuality: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        torchMode: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        cameraType: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        rotateMode: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        model: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        distance: PropTypes.oneOfType([
            PropTypes.string,
            PropTypes.number
        ]),
        
        touchToFocus: PropTypes.bool,
        onTrained: PropTypes.func,
        onRecognized: PropTypes.func,
        onFaceCaptured: PropTypes.func,
        onWpcButtonClicked: PropTypes.func,
        onUntrained: PropTypes.func,
        onUnrecognized: PropTypes.func
    };

    static defaultProps: Object = {
        aspect: WpcButtonManager.Aspect.fill,
        captureQuality: WpcButtonManager.CaptureQuality.medium,
        torchMode: WpcButtonManager.TorchMode.off,
        cameraType: WpcButtonManager.CameraType.back,
        model: WpcButtonManager.Model.cascade,
        rotateMode: WpcButtonManager.RotateMode.off,

        touchToFocus: false,
        distance: 200
    };

    _cameraRef: ?Object;
    _cameraHandle: ?number;

    _setReference = (ref: ?Object) => {
        console.log("_setReference");
        // console.log(ref);
        
        if (ref) {
            console.log(" NICE ");
            this._cameraRef = ref;
            this._cameraHandle = findNodeHandle(ref);
        } else {
            console.log(" NULL ");
            this._cameraRef = null;
            this._cameraHandle = null;
        }
    };

    constructor() {
        console.log("constructor");
        super();
        this.state = {
            mounted: false
        };
    }

    onTrained = () => {
        if (this.props.onTrained) {
            this.props.onTrained();
        }
    }
    onUntrained = (event) => {
        if (this.props.onUntrained) {
            this.props.onUntrained(event.nativeEvent);
        }
    }

    onRecognized = (event) => {
        if (this.props.onRecognized) {
            this.props.onRecognized(event.nativeEvent);
        }
    }

    onFaceCaptured = (event) => {
        if (this.props.onFaceCaptured) {
            this.props.onFaceCaptured(event.nativeEvent);
        }
    }

    onWpcButtonClicked = (event) => {
        if (this.props.onWpcButtonClicked) {
            this.props.onWpcButtonClicked(event.nativeEvent);
        }
    }

    onUnrecognized = (event) => {
        if (this.props.onUnrecognized) {
            this.props.onUnrecognized(event.nativeEvent);
        }
    }

    componentDidMount() {
        this.setState({
            mounted: true
        })

    }

    componentWillUnmount() {
        this.setState({
            mounted: false
        })
    }
    
    // customize
    wpcBtnClick() {
        WpcButtonManager.wpcBtnClick(this._cameraHandle);
    }

    wpcConnectToPrinter() {
        WpcButtonManager.wpcConnectToPrinter(this._cameraHandle);
    }
    
    async takePicture() {
        return await WpcButtonManager.detection(this._cameraHandle);
    }

    train(info?: DetectOption) {
        WpcButtonManager.train(info, this._cameraHandle);
    }

    identify() {
        WpcButtonManager.recognize(this._cameraHandle);
    }

    clear() {
        WpcButtonManager.clear(this._cameraHandle);
    }

    render() {
        const nativeProps = convertNativeProps(this.props);
        console.log(" index.js render() ");
        // console.log(" nativeProps= ");
        // console.log(nativeProps);
        return <WpcButton
            mounted={this.state.mounted}
            ref={this._setReference}
            {...nativeProps}
            onTrained={this.onTrained}
            onRecognized={this.onRecognized}
            onFaceCaptured={this.onFaceCaptured}
            onWpcButtonClicked={this.onWpcButtonClicked}
            onUntrained={this.onUntrained}
            onUnrecognized={this.onUnrecognized}/>;
    }
}
export const constants = WpcButtonClass.constants;
const WpcButton = requireNativeComponent(
    'WpcButton',
    WpcButtonClass,
    {
        nativeOnly: {
            'mounted': true,
        }
    }
);
console.log(" bottom line ");


