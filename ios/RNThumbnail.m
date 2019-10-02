
#import "RNThumbnail.h"
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVAsset.h>
#import <UIKit/UIKit.h>

@implementation RNThumbnail

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(get:(NSString *)filepath config:(NSDictionary *)config
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    @try {
        NSURL *vidURL = NULL;
        AVURLAsset *asset = NULL;
        if([filepath containsString:@"file://"]){
            filepath = [filepath stringByReplacingOccurrencesOfString:@"file://"
            withString:@""];
            vidURL = [NSURL fileURLWithPath:filepath];
            asset = [[AVURLAsset alloc] initWithURL:vidURL options:nil];
        } else {
            vidURL = [NSURL URLWithString:filepath];
            asset = [[AVURLAsset alloc] initWithURL:vidURL options:@{@"AVURLAssetHTTPHeaderFieldsKey":[config objectForKey:@"headers"]}];
        }
                
        AVAssetImageGenerator *generator = [[AVAssetImageGenerator alloc] initWithAsset:asset];
        generator.appliesPreferredTrackTransform = YES;
        
        NSError *err = NULL;
        
        
        CMTime time = CMTimeMake([[config objectForKey:@"timeFrame"] intValue], 60);
        
        CGImageRef imgRef = [generator copyCGImageAtTime:time actualTime:NULL error:&err];
        UIImage *thumbnail = [UIImage imageWithCGImage:imgRef];
        
        NSString *base64String = [UIImagePNGRepresentation(thumbnail)
        base64EncodedStringWithOptions:NSDataBase64Encoding64CharacterLineLength];
        if (resolve)
            resolve(@{ @"data" : base64String,
                       @"width" : [NSNumber numberWithFloat: thumbnail.size.width],
                       @"height" : [NSNumber numberWithFloat: thumbnail.size.height] });
    } @catch(NSException *e) {
        reject(e.reason, nil, nil);
    }
}


@end
